package com.aquip.tetris;

import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.handler.DeathHandler;
import com.aquip.tetris.input.InputFrame;
import com.aquip.tetris.menu.MenuEngine;
import com.aquip.tetris.menu.MenuInputMapper;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.ConfigState;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.UUID;

public final class VerificationSmoke {

    public static void main(String[] args) {
        verifyMenuStartsSinglePlayer();
        verifyGravityCurve();
        verifySpawnCollisionDeath();
        verifyHiddenRowLockOutDeath();
        System.out.println("Smoke verification passed.");
    }

    private static void verifyMenuStartsSinglePlayer() {
        MenuEngine menu = new MenuEngine(
                new File("config/config.yml"),
                new MenuInputMapper()
        );

        InputFrame frame = new InputFrame();
        frame.pressed.add(KeyEvent.VK_ENTER);

        GameEngine game = menu.update(frame);

        require(game != null, "Menu did not start a game");
        require(game.getMatchState().players.size() == 1, "Menu did not create a single-player match");
        require(game.getMatchState().players.get(0).player.getType() == PlayerType.HUMAN, "Primary player is not human");
    }

    private static void verifyGravityCurve() {
        ConfigState config = new ConfigState();

        require(config.gravityThresholdForPieces(0) == 60, "Base gravity threshold is wrong");
        require(config.gravityThresholdForPieces(50) == 40, "Gravity threshold does not accelerate");
        require(config.gravityThresholdForPieces(200) == 8, "Gravity threshold does not clamp to minimum");
        require(config.softDropThresholdForPieces(500) == 1, "Soft drop threshold should bottom out at 1");
    }

    private static void verifySpawnCollisionDeath() {
        PlayerState player = createPlayerState();
        MatchState match = new MatchState();
        match.addPlayer(player);

        player.piece.currentPiece = new Piece(PieceType.O, 0, 4, 0);
        player.board.set(4, 0, 1);

        TickContext context = new TickContext();
        context.reset(List.of(player));
        context.get(player).pieceSpawned = true;

        new DeathHandler().apply(match, context);

        require(!player.status.alive, "Spawn collision should kill the player");
        require(player.piece.currentPiece == null, "Killed player should not keep an active piece");
    }

    private static void verifyHiddenRowLockOutDeath() {
        PlayerState player = createPlayerState();
        MatchState match = new MatchState();
        match.addPlayer(player);

        player.board.set(0, 0, 1);

        TickContext context = new TickContext();
        context.reset(List.of(player));
        context.get(player).piecePlaced = true;

        new DeathHandler().apply(match, context);

        require(!player.status.alive, "Hidden-row lock out should kill the player");
    }

    private static PlayerState createPlayerState() {
        Player player = new Player(UUID.randomUUID(), PlayerType.HUMAN, "P1");
        return new PlayerState(player, new ConfigState());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
