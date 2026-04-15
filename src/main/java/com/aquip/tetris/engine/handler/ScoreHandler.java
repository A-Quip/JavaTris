package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.PlayerTickContext;
import com.aquip.tetris.state.PlayerState;
import com.aquip.tetris.placement.PlacementResult;
import com.aquip.tetris.placement.PlaceKey;
import com.aquip.tetris.placement.SpinResult;

public class ScoreHandler implements PlayerHandler {

    @Override
    public void apply(PlayerState player, TickContext context) {

        PlayerTickContext ctx = context.get(player);

        // Only score when a piece was placed this tick
        if (!ctx.pieceSpawned) return;

        PlacementResult result = getLastPlacement(player);
        if (result == null) return;

        PlaceKey key = result.getPlaceKey();

        // =====================
        // 1. COMBO
        // =====================
        if (result.lines > 0) {
            player.combo.currentCombo.add(result);
        } else {
            player.combo.currentCombo.clear();
        }

        // =====================
        // 2. B2B (Back-to-Back)
        // =====================
        //System.out.println(key);
        if (isB2BEligible(key)) {
            player.b2b.currentB2B.add(result);
        } else if (result.lines > 0) {
            player.b2b.currentB2B.clear();
        }

        // =====================
        // 3. SCORE CALCULATION
        // =====================
        int baseScore = player.config.scoreTable.get(key);

        int comboBonus = Math.max(0, player.combo.amount() - 1) * 50;

        double b2bMultiplier = (player.b2b.amount() > 1) ? 1.5 : 1.0;

        int finalScore = (int) ((baseScore + comboBonus)
                * b2bMultiplier
                * player.config.scoreMult);

        // =====================
        // 4. APPLY SCORE
        // =====================
        player.status.Score += finalScore;
    }

    // =====================
    // HELPERS
    // =====================
    private PlacementResult getLastPlacement(PlayerState player) {
        if (player.time.piecesPlaced.isEmpty()) return null;
        return player.time.piecesPlaced.get(player.time.piecesPlaced.size() - 1);
    }

    private boolean isB2BEligible(PlaceKey key) {
        boolean tetris = key.lines == 4;
        boolean spin = key.spin != SpinResult.NONE;

        return spin || tetris;
    }
}