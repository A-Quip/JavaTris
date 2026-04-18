package com.aquip.tetris.ai.search;

import com.aquip.tetris.ai.sim.FastBoard;
import com.aquip.tetris.ai.sim.FastPhysics;
import com.aquip.tetris.ai.sim.FastGravity;
import com.aquip.tetris.input.GameInput;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.state.ConfigState;
import com.aquip.tetris.state.LockState;
import com.aquip.tetris.state.GravityState;

import java.util.*;

/**
 * Single-piece path discovery using Breadth-First Search (BFS).
 * Detects all possible legal placements for a piece from a given starting
 * state. Optimized for performance by pruning spatial duplicates.
 *
 * boolean visited array: The BFS visited set is bounded by
 * rotation × y × x, which is at most ~1,800 states. A flat
 * {@code boolean[]} indexed by a packed int is vastly faster than a
 * {@code HashSet<BFSNode>}: no boxing, no hash computation, no collision
 * chains, and a trivial {@code Arrays.fill} reset.
 * ArrayDeque queue: {@code LinkedList} allocates a
 * {@code Node} wrapper for every enqueue. {@code ArrayDeque} is
 * array-backed, cache-friendly, and allocation-free for queue
 * operations.
 * int placement key: Deduplifying final placements with
 * {@code String} concatenation creates a new {@code String} object per
 * candidate. Encoding (rotation, y, x) into a single {@code int} and
 * using a {@code boolean[]} removes all string allocation.
 */
public class BFSPathFinder {

    private final ConfigState config;

    // --- State-space bounds for the visited / placement arrays -----
    // Covers the full reachable position space including SRS kick offsets.
    // x: -3 to +13 → X_RANGE = 17, X_OFFSET = 3
    // y: -3 to +26 → Y_RANGE = 30, Y_OFFSET = 3
    // rotation: 0-3 → ROT_RANGE = 4
    // Total array size: 4 × 30 × 17 = 2040 booleans ≈ 2 KB per thread
    private static final int X_OFFSET = 3;
    private static final int X_RANGE = 17;
    private static final int Y_OFFSET = 3;
    private static final int Y_RANGE = 30;
    private static final int ROT_RANGE = 4;
    private static final int STATE_ARRAY_SIZE = ROT_RANGE * Y_RANGE * X_RANGE;

    /**
     * Per-thread visited set for the BFS. Cleared and reused on every
     * {@link #findAll} call — no per-call allocation.
     */
    private static final ThreadLocal<boolean[]> VISITED = ThreadLocal.withInitial(() -> new boolean[STATE_ARRAY_SIZE]);

    /**
     * Per-thread set tracking already-emitted final placements. Same
     * encoding as {@link #VISITED}.
     */
    private static final ThreadLocal<boolean[]> PLACEMENTS = ThreadLocal
            .withInitial(() -> new boolean[STATE_ARRAY_SIZE]);

    // -------------------------------------------------------------------------

    public BFSPathFinder(ConfigState config) {
        this.config = config;
    }

    /**
     * Represents a valid final placement of a piece discovered by the BFS.
     */
    public static class Placement {
        public final Piece finalPiece;
        public final List<GameInput> commands;
        public final Piece[] expectedPositions;

        public Placement(Piece finalPiece, List<GameInput> commands, List<Piece> positions) {
            this.finalPiece = finalPiece;
            this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
            this.expectedPositions = positions.toArray(new Piece[0]);
        }
    }

    // BFSNode no longer needs equals/hashCode — the boolean[] array handles
    // all visited-state checks before a node is ever created, so BFSNode
    // instances are never placed in a HashSet.
    private static class BFSNode {
        final Piece piece;
        final int gravityTicks;
        final int lockTicks;
        final int slides;
        final int rotations;
        final int lowestY;
        final BFSNode parent;
        final GameInput input;

        BFSNode(Piece piece, int gravityTicks, int lockTicks,
                int slides, int rotations, int lowestY,
                BFSNode parent, GameInput input) {
            this.piece = piece;
            this.gravityTicks = gravityTicks;
            this.lockTicks = lockTicks;
            this.slides = slides;
            this.rotations = rotations;
            this.lowestY = lowestY;
            this.parent = parent;
            this.input = input;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers for the bounded state array

    /** Encodes (rotation, y, x) as a flat array index. No bounds check. */
    private static int stateIndex(int x, int y, int rotation) {
        return (rotation * Y_RANGE + (y + Y_OFFSET)) * X_RANGE + (x + X_OFFSET);
    }

    /** Returns true when the position fits inside the pre-allocated array. */
    private static boolean inRange(int x, int y, int rotation) {
        return rotation >= 0 && rotation < ROT_RANGE
                && (x + X_OFFSET) >= 0 && (x + X_OFFSET) < X_RANGE
                && (y + Y_OFFSET) >= 0 && (y + Y_OFFSET) < Y_RANGE;
    }

    // -------------------------------------------------------------------------

    /**
     * Finds all possible legal placements for the current piece.
     *
     * @param board        The current bitboard.
     * @param currentPiece The initial piece state.
     * @param lock         Current lock timer state.
     * @param gravity      Current gravity accumulator state.
     * @param piecesPlaced Total pieces placed for gravity scaling.
     * @return A list of unique legal placements.
     */
    public List<Placement> findAll(FastBoard board, Piece currentPiece,
            LockState lock, GravityState gravity, int piecesPlaced, int ticksPerCommand) {

        // Acquire and reset per-thread arrays (#1, #3)
        boolean[] visited = VISITED.get();
        boolean[] placements = PLACEMENTS.get();
        Arrays.fill(visited, false);
        Arrays.fill(placements, false);

        List<Placement> results = new ArrayList<>();
        Queue<BFSNode> queue = new ArrayDeque<>();

        BFSNode root = new BFSNode(currentPiece,
                gravity.gravityTicks, lock.lockTicks,
                lock.slides, lock.rotations, lock.lowestY,
                null, null);

        // Mark root visited before enqueueing
        if (inRange(currentPiece.x, currentPiece.y, currentPiece.rotation)) {
            visited[stateIndex(currentPiece.x, currentPiece.y, currentPiece.rotation)] = true;
        }
        queue.add(root);

        while (!queue.isEmpty()) {
            BFSNode node = queue.poll();

            boolean grounded = FastPhysics.collides(board, node.piece.displace(0, 1));

            // --- TERMINAL STATES ---
            Piece dropped = FastPhysics.hardDrop(board, node.piece);
            addPlacementIfUnique(results, placements, node, GameInput.HARD_DROP, dropped);

            if (grounded && node.lockTicks >= config.lockTick) {
                addPlacementIfUnique(results, placements, node, null, node.piece);
                continue;
            }

            // --- TRANSITIONS ---
            tryMove(board, node, -1, 0, GameInput.MOVE_LEFT, queue, visited, grounded, ticksPerCommand);
            tryMove(board, node, 1, 0, GameInput.MOVE_RIGHT, queue, visited, grounded, ticksPerCommand);
            tryRotate(board, node, 1, GameInput.ROTATE_CW, queue, visited, grounded, ticksPerCommand);
            tryRotate(board, node, -1, GameInput.ROTATE_CCW, queue, visited, grounded, ticksPerCommand);
            tryRotate(board, node, 2, GameInput.ROTATE_180, queue, visited, grounded, ticksPerCommand);
            tryGravity(board, node, false, queue, visited, piecesPlaced, grounded, ticksPerCommand);
            tryGravity(board, node, true, queue, visited, piecesPlaced, grounded, ticksPerCommand);
        }

        return results;
    }

    // -------------------------------------------------------------------------

    private void tryMove(FastBoard board, BFSNode node, int dx, int dy,
            GameInput input, Queue<BFSNode> queue, boolean[] visited,
            boolean groundedBefore, int ticksPerCommand) {
        Piece next = FastPhysics.applyMove(board, node.piece, dx, dy);
        if (next != null) {
            processNextNode(node, next, input, queue, visited,
                    groundedBefore, true, false, node.gravityTicks + ticksPerCommand);
        }
    }

    private void tryRotate(FastBoard board, BFSNode node, int step,
            GameInput input, Queue<BFSNode> queue, boolean[] visited,
            boolean groundedBefore, int ticksPerCommand) {
        Piece next = FastPhysics.tryRotate(board, node.piece, step);
        if (next != null) {
            processNextNode(node, next, input, queue, visited,
                    groundedBefore, false, true, node.gravityTicks + ticksPerCommand);
        }
    }

    private void tryGravity(FastBoard board, BFSNode node, boolean isSoftDrop,
            Queue<BFSNode> queue, boolean[] visited,
            int piecesPlaced, boolean groundedBefore, int ticksPerCommand) {
        int nextGravityTicks = node.gravityTicks + ticksPerCommand;
        Piece nextPiece = node.piece;

        if (FastGravity.shouldFall(nextGravityTicks, isSoftDrop, config, piecesPlaced)) {
            Piece fallen = FastPhysics.applyMove(board, node.piece, 0, 1);
            if (fallen != null) {
                nextPiece = fallen;
            }
            nextGravityTicks = 0;
        }

        processNextNode(node, nextPiece, isSoftDrop ? GameInput.SOFT_DROP : GameInput.NONE,
                queue, visited, groundedBefore, false, false, nextGravityTicks);
    }

    /**
     * Computes the next lock/gravity state and, if the resulting spatial position
     * has not been visited yet, creates a BFSNode and enqueues it.
     *
     * <p>
     * The visited check happens <em>before</em> object creation, so a BFSNode
     * is only allocated when the state is genuinely new.
     * </p>
     */
    private void processNextNode(BFSNode node, Piece next, GameInput input,
            Queue<BFSNode> queue, boolean[] visited,
            boolean groundedBefore, boolean moved, boolean rotated,
            int nextGravityTicks) {

        int nextLockTicks = node.lockTicks;
        int nextSlides = node.slides;
        int nextRotations = node.rotations;
        int nextLowestY = node.lowestY;

        // Lowest Y Reset
        if (next.y > nextLowestY) {
            nextLowestY = next.y;
            nextLockTicks = 0;
            nextSlides = 0;
            nextRotations = 0;
        } else if (groundedBefore) {
            // Limit-based Reset
            boolean reset = false;
            if (moved && nextSlides < config.maxSlides) {
                nextSlides++;
                reset = true;
            }
            if (rotated && nextRotations < config.maxRotations) {
                nextRotations++;
                reset = true;
            }
            nextLockTicks = reset ? 0 : nextLockTicks + 1;
        } else {
            nextLockTicks = 0;
        }

        // Check the boolean array before allocating a BFSNode.
        // If out of the pre-allocated range (degenerate SRS edge case), fall
        // through and create the node anyway — the lock timer will still
        // terminate the BFS.
        if (inRange(next.x, next.y, next.rotation)) {
            int idx = stateIndex(next.x, next.y, next.rotation);
            if (visited[idx])
                return;
            visited[idx] = true;
        }

        queue.add(new BFSNode(next, nextGravityTicks, nextLockTicks,
                nextSlides, nextRotations, nextLowestY, node, input));
    }

    /**
     * Records a placement if it hasn't been seen before in this BFS call.
     *
     * Uses the same int-encoded key as the visited array instead of
     * building a {@code String} per candidate, eliminating all string
     * allocation in the hot path.
     */
    private void addPlacementIfUnique(List<Placement> results,
            boolean[] placements, BFSNode node,
            GameInput terminalInput, Piece finalPiece) {

        // #3: Encode (rotation, y, x) as a single int — no String allocation
        if (!inRange(finalPiece.x, finalPiece.y, finalPiece.rotation))
            return;
        int key = stateIndex(finalPiece.x, finalPiece.y, finalPiece.rotation);
        if (placements[key])
            return;
        placements[key] = true;

        List<GameInput> commands = new ArrayList<>();
        List<Piece> positions = new ArrayList<>();

        if (terminalInput != null) {
            commands.add(terminalInput);
        }

        BFSNode curr = node;
        while (curr != null) {
            positions.add(curr.piece);
            if (curr.input != null) {
                commands.add(curr.input);
            }
            curr = curr.parent;
        }

        Collections.reverse(commands);
        Collections.reverse(positions);

        results.add(new Placement(finalPiece, commands, positions));
    }
}