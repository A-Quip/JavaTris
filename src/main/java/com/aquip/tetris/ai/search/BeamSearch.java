package com.aquip.tetris.ai.search;

import com.aquip.tetris.ai.AIConfig;
import com.aquip.tetris.ai.AIGameSnapshot;
import com.aquip.tetris.ai.eval.Heuristic;
import com.aquip.tetris.ai.sim.FastBoard;
import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.state.GravityState;
import com.aquip.tetris.state.LockState;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Multi-piece beam search coordinator.
 * Explores possible move sequences for the current and upcoming pieces,
 * selecting the path that results in the most favorable board state.
 *
 * Parallel beam expansion</b>: Each beam node's BFS search is
 * completely independent of all others (immutable board input, stateless
 * pathfinder, thread-local scratch buffers). A dedicated
 * {@link ForkJoinPool} runs all node expansions in parallel at each
 * depth level. Results are merged sequentially after the parallel phase
 * to avoid any lock contention on the deduplication map.
 * Cached heuristic score: {@code heuristic.evaluate()} was
 * previously called O(n log n) times during sorting and again for
 * logging. The score is now computed once per node during the parallel
 * expansion phase and stored in {@link SimNode#cachedScore}, making all
 * subsequent sort/log operations trivially cheap.
 */
public class BeamSearch {

    private final BFSPathFinder pathFinder;
    private final Heuristic heuristic;
    private final AIConfig config;

    // #7: Dedicated pool for parallel node expansion.
    // We leave at least one core free for the game thread (and one for the
    // ai-search thread that calls findBestPath), so parallelism = cores - 2,
    // clamped to at least 1.
    private final ForkJoinPool searchPool;

    public BeamSearch(BFSPathFinder pathFinder, Heuristic heuristic, AIConfig config) {
        this.pathFinder = pathFinder;
        this.heuristic = heuristic;
        this.config = config;
        int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
        this.searchPool = new ForkJoinPool(parallelism);
    }

    /**
     * Shuts down the internal thread pool. Called from
     * {@link com.aquip.tetris.ai.AIController#shutdown()}.
     */
    public void shutdown() {
        searchPool.shutdown();
    }

    /**
     * Executes the multi-piece beam search.
     *
     * @param snapshot The current immutable game snapshot.
     * @return The best immediate MoveSequence to execute.
     */
    public MoveSequence findBestPath(AIGameSnapshot snapshot) {

        config.validateQueueDepth(snapshot.nextQueue.length);

        List<SimNode> beam = new ArrayList<>();
        FastBoard initialBoard = FastBoard.fromSnapshot(snapshot.board);

        // Initial placements for current piece
        List<BFSPathFinder.Placement> currentPlacements = pathFinder.findAll(
                initialBoard, snapshot.currentPiece,
                getLockState(snapshot), getGravityState(snapshot),
                snapshot.piecesPlacedCount, config.ticksPerCommand());

        for (BFSPathFinder.Placement p : currentPlacements) {
            MoveSequence path = new MoveSequence(p.commands, p.expectedPositions, snapshot.piecesPlacedCount);
            beam.add(createInitialNode(initialBoard, snapshot.nextQueue, 0, p, path, true,
                    snapshot.comboCount, snapshot.b2bCount));
        }

        // Consider HOLD path
        if (snapshot.canHold) {
            PieceType heldType = (snapshot.heldPiece != null) ? snapshot.heldPiece : snapshot.nextQueue[0];
            int nextQueueIndex = (snapshot.heldPiece != null) ? 0 : 1;

            Piece heldPiece = new Piece(heldType, 0, 3, 3);
            List<BFSPathFinder.Placement> heldPlacements = pathFinder.findAll(
                    initialBoard, heldPiece, new LockState(), new GravityState(),
                    snapshot.piecesPlacedCount, config.ticksPerCommand());

            for (BFSPathFinder.Placement p : heldPlacements) {
                MoveSequence holdOnlyPath = new MoveSequence(
                        Collections.singletonList(GameInput.HOLD_PIECE),
                        new Piece[0],
                        snapshot.piecesPlacedCount);
                beam.add(createInitialNode(initialBoard, snapshot.nextQueue, nextQueueIndex, p,
                        holdOnlyPath, false, snapshot.comboCount, snapshot.b2bCount));
            }
        }

        if (beam.isEmpty()) {
            System.out.println("[Search] CRITICAL: No placements found for current piece!");
            return MoveSequence.HARD_DROP_NOW;
        }

        // Each depth level fans out into one Callable per beam node.
        int maxDepth = config.lookaheadDepth();

        for (int depth = 1; depth <= maxDepth; depth++) {

            // Capture loop variables for lambda capture (both are mutated by the loop)
            final List<SimNode> currentBeam = beam;
            final int capturedDepth = depth;

            // Build one expansion task per beam node
            List<Callable<List<SimNode>>> expansionTasks = new ArrayList<>(currentBeam.size());
            for (SimNode node : currentBeam) {
                if (node.currentPiece == null)
                    continue;
                expansionTasks.add(() -> {
                    List<SimNode> nodeResults = new ArrayList<>();
                    List<BFSPathFinder.Placement> placements = pathFinder.findAll(
                            node.board,
                            node.currentPiece,
                            node.lockState,
                            node.gravityState,
                            snapshot.piecesPlacedCount + capturedDepth,
                            config.ticksPerCommand());

                    for (BFSPathFinder.Placement p : placements) {
                        SimNode next = node.applyPlacement(p, snapshot.nextQueue);
                        // Compute score once here, in the parallel phase,
                        // so all subsequent sort/dedup/log calls are free.
                        next.cachedScore = heuristic.evaluate(next);
                        nodeResults.add(next);
                    }
                    return nodeResults;
                });
            }

            // Execute in parallel, then merge sequentially to prevent lock contention
            Map<FastBoard, SimNode> nextBeamMap = new HashMap<>();
            try {
                List<Future<List<SimNode>>> futures = searchPool.invokeAll(expansionTasks);
                for (Future<List<SimNode>> future : futures) {
                    try {
                        for (SimNode nextNode : future.get()) {
                            SimNode existing = nextBeamMap.get(nextNode.board);
                            if (existing == null || nextNode.totalLinesCleared > existing.totalLinesCleared) {
                                nextBeamMap.put(nextNode.board, nextNode);
                            }
                        }
                    } catch (ExecutionException e) {
                        System.err.println("[Search] Warning: expansion task failed: " + e.getCause());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[Search] Beam expansion interrupted at depth " + depth);
                break;
            }

            List<SimNode> nextBeam = new ArrayList<>(nextBeamMap.values());
            if (nextBeam.isEmpty())
                break;

            // Sort using the cached score — O(n log n) comparisons
            // If scores are tied, prefer the path with fewer commands
            nextBeam.sort((a, b) -> {
                int cmp = Double.compare(b.cachedScore, a.cachedScore);
                if (cmp != 0)
                    return cmp;
                // Primary tie-breaker: Fewer total commands in the sequence
                return Integer.compare(a.rootPath.commands.size(), b.rootPath.commands.size());
            });
            int limit = Math.min(config.beamWidth(), nextBeam.size());
            beam = new ArrayList<>(nextBeam.subList(0, limit));
        }

        // Final sort: scores should already be cached from the last depth's parallel
        // expansion, but guard for the edge case where the loop never ran
        // (e.g. maxDepth=0, or every node had a null piece).
        for (SimNode n : beam) {
            if (Double.isNaN(n.cachedScore)) {
                n.cachedScore = heuristic.evaluate(n);
            }
        }
        beam.sort((a, b) -> {
            int cmp = Double.compare(b.cachedScore, a.cachedScore);
            if (cmp != 0)
                return cmp;
            return Integer.compare(a.rootPath.commands.size(), b.rootPath.commands.size());
        });

        MoveSequence best = beam.get(0).rootPath;
        // System.out.println("[Search] Result: " + best.commands + " (Score: " +
        // beam.get(0).cachedScore + ")");
        return best;
    }

    // -------------------------------------------------------------------------

    private SimNode createInitialNode(FastBoard board, PieceType[] queue,
            int nextQueueIdx, BFSPathFinder.Placement p,
            MoveSequence path, boolean canHold, int combo, int b2b) {
        SimNode root = new SimNode(board, path, nextQueueIdx,
                null, new LockState(), new GravityState(), canHold, 0, combo, b2b > 0);
        return root.applyPlacement(p, queue);
    }

    private LockState getLockState(AIGameSnapshot snapshot) {
        LockState ls = new LockState();
        ls.lockTicks = snapshot.lockTicks;
        ls.slides = snapshot.lockSlides;
        ls.rotations = snapshot.lockRotations;
        ls.lowestY = snapshot.lowestY;
        return ls;
    }

    private GravityState getGravityState(AIGameSnapshot snapshot) {
        GravityState gs = new GravityState();
        gs.gravityTicks = snapshot.gravityTicks;
        gs.softDrop = snapshot.gravitySoftDrop;
        return gs;
    }
}