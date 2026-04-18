package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.PlayerTickContext;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.placement.PlacementResult;
import com.aquip.tetris.placement.PlaceKey;

import java.util.UUID;

public class GarbageHandler {

    public void apply(MatchState match, TickContext context) {

        if (match.players.size() < 2) {
            return;
        }

        for (PlayerState attacker : match.players) {
            if (!attacker.status.alive) {
                continue;
            }

            validateTarget(attacker, match);

            PlayerTickContext ctx = context.get(attacker);

            if (!ctx.piecePlaced) continue;

            PlacementResult result = getLastPlacement(attacker);
            if (result == null) {
                continue;
            }

            PlaceKey key = result.getPlaceKey();

            // =====================
            // CALCULATE
            // =====================
            int garbage = attacker.config.garbageTable.get(key);

            if (key.lines > 0) {
                int comboBonus = Math.max(0, (attacker.combo.amount()) / 2);
                garbage += comboBonus;

                int b2bBonus = Math.min(4, attacker.b2b.amount());
                garbage += b2bBonus;
            }

            if (garbage <= 0) {
                continue;
            }

            // =====================
            // CANCEL
            // =====================
            int remaining = cancelIncoming(attacker, garbage);

            if (remaining <= 0) continue;

            // =====================
            // TARGET
            // =====================
            PlayerState target = findTarget(match, attacker);

            if (target == null) {
                continue;
            }

            // =====================
            // SEND
            // =====================
            int hole = (int) (Math.random() * target.config.boardWidth);
            target.garbage.incoming.add(remaining, hole);
        }
    }

    private PlacementResult getLastPlacement(PlayerState player) {
        if (player.time.piecesPlaced.isEmpty()) return null;
        return player.time.piecesPlaced.get(player.time.piecesPlaced.size() - 1);
    }

    private int cancelIncoming(PlayerState player, int garbage) {

        if (player.garbage.incoming == null) return garbage;

        int cancelled = player.garbage.incoming.poll(garbage);

        return garbage - cancelled;
    }

    private PlayerState findTarget(MatchState match, PlayerState attacker) {

        if (attacker.garbage.target == null) return null;

        for (PlayerState p : match.players) {
            if (p.player == null) continue;

            if (attacker.garbage.target.equals(p.player.getId())) {
                return p;
            }
        }

        return null;
    }

    private void validateTarget(PlayerState attacker, MatchState match) {

        // If current target is still valid → do nothing
        if (isValidTarget(attacker, match, attacker.garbage.target)) {
            return;
        }

        // Otherwise find valid targets
        var validTargets = match.players.stream()
                .filter(p -> p != attacker)
                .filter(p -> p.status.alive)
                .toList();

        if (validTargets.isEmpty()) {
            attacker.garbage.target = null;
            return;
        }

        // Pick random new target
        PlayerState newTarget = validTargets.get((int)(Math.random() * validTargets.size()));
        attacker.garbage.target = newTarget.player.getId();
    }

    private boolean isValidTarget(PlayerState attacker, MatchState match, UUID targetId) {
        if (targetId == null) return false;

        for (PlayerState p : match.players) {
            if (p == attacker) continue;
            if (!p.status.alive) continue;

            if (targetId.equals(p.player.getId())) {
                return true;
            }
        }

        return false;
    }
}
