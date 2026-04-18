package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.PlayerTickContext;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceType;

import java.util.*;

public class QueueHandler {

    private final Random random = new Random();

    public void apply(MatchState match, TickContext context) {

        for (PlayerState player : match.players) {
            if (!player.status.alive) {
                continue;
            }

            PlayerTickContext ctx = context.get(player);

            // =====================
            // 1. HOLD
            // =====================
            if (ctx.hold && player.config.allowHold) {
                handleHold(player);
            }

            // =====================
            // 2. SPAWN
            // =====================
            if (ctx.pieceSpawned || !player.piece.hasPiece()) {
                spawnNext(player);
            }

            // reset movecontext
            if (ctx.pieceSpawned) {
                ctx.resetPiece();
            }

            // =====================
            // 3. MAINTAIN QUEUE
            // =====================
            refillQueue(player);
        }
    }

    // =====================
    // HOLD LOGIC
    // =====================
    private void handleHold(PlayerState player) {

        if (!player.next.canHold)
            return;

        Piece current = player.piece.currentPiece;
        if (current == null)
            return;

        PieceType held = player.next.held;

        // First hold → store and spawn new
        if (held == null) {
            player.next.held = current.type;
            player.piece.currentPiece = null;
            player.next.canHold = false;
            spawnNext(player);
            return;
        }

        // Swap
        player.next.held = current.type;
        player.piece.currentPiece = createSpawnPiece(player, held);
        player.next.canHold = false;
        resetSpawnState(player);
    }

    // =====================
    // SPAWN LOGIC
    // =====================
    private void spawnNext(PlayerState player) {

        if (player.next.next.isEmpty()) {
            refillQueue(player);
        }

        PieceType nextType = player.next.next.poll();
        player.piece.currentPiece = createSpawnPiece(player, nextType);
        resetSpawnState(player);
    }

    private Piece createSpawnPiece(PlayerState player, PieceType type) {

        int spawnX = player.config.boardWidth / 2 - 2;
        int spawnY = 3;

        return new Piece(type, 0, spawnX, spawnY);
    }

    // =====================
    // QUEUE REFILL
    // =====================
    private void refillQueue(PlayerState player) {

        Queue<PieceType> queue = player.next.next;

        while (queue.size() < player.config.nextSize) {

            if (player.config.useBag) {
                addBag(queue);
            } else {
                queue.add(randomPiece());
            }
        }
    }

    // =====================
    // BAG RANDOMIZER (7-bag)
    // =====================
    private void addBag(Queue<PieceType> queue) {

        List<PieceType> bag = new ArrayList<>(Arrays.asList(PieceType.values()));
        Collections.shuffle(bag, random);
        queue.addAll(bag);
    }

    private PieceType randomPiece() {
        PieceType[] values = PieceType.values();
        return values[random.nextInt(values.length)];
    }

    private void resetSpawnState(PlayerState player) {
        player.lock.reset();
        player.gravity.reset();
    }
}
