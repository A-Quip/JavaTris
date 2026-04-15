package com.aquip.tetris;

import com.aquip.tetris.ai.HeuristicAI;
import com.aquip.tetris.engine.GameEngine;
import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.engine.handler.DeathHandler;
import com.aquip.tetris.input.InputFrame;
import com.aquip.tetris.input.PlayerInput;
import com.aquip.tetris.ui.menu.GameMode;
import com.aquip.tetris.ui.menu.MenuEngine;
import com.aquip.tetris.ui.menu.MenuInputMapper;
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
        verifyMenuModesCreateExpectedPlayers();
        verifyGravityCurve();
        verifyVersusGameOverRules();
        verifyAiProducesPlannedInput();
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

    private static void verifyMenuModesCreateExpectedPlayers() {
        MenuEngine menu = new MenuEngine(
                new File("config/config.yml"),
                new MenuInputMapper()
        );

        menu.state.selectionIndex = GameMode.VS_AI.ordinal();
        GameEngine vsAi = menu.createGame();
        require(vsAi.getMatchState().players.size() == 2, "Vs AI should create two players");
        require(vsAi.getMatchState().players.get(1).player.getType() == PlayerType.AI, "Vs AI should create an AI opponent");

        menu.state.selectionIndex = GameMode.TWO_PLAYER.ordinal();
        GameEngine twoPlayer = menu.createGame();
        require(twoPlayer.getMatchState().players.size() == 2, "2 Players mode should create two players");
        require(twoPlayer.getMatchState().players.get(1).player.getType() == PlayerType.HUMAN, "2 Players mode should create a second human");

        menu.state.selectionIndex = GameMode.AI_DEMO.ordinal();
        GameEngine aiDemo = menu.createGame();
        require(aiDemo.getMatchState().players.size() == 1, "AI Demo should create one player");
        require(aiDemo.getMatchState().players.get(0).player.getType() == PlayerType.AI, "AI Demo should create an AI player");
    }

    private static void verifyGravityCurve() {
        ConfigState config = new ConfigState();

        require(config.gravityThresholdForPieces(0) == 60, "Base gravity threshold is wrong");
        require(config.gravityThresholdForPieces(50) < config.gravityThresholdForPieces(0), "Gravity threshold does not accelerate");
        require(config.gravityThresholdForPieces(2_000) == config.minimumGravityTick, "Gravity threshold does not clamp to minimum");
        require(config.softDropThresholdForPieces(500) == 1, "Soft drop threshold should bottom out at 1");
    }

    private static void verifyVersusGameOverRules() {
        MatchState match = new MatchState();
        PlayerState p1 = createPlayerState("P1", PlayerType.HUMAN);
        PlayerState p2 = createPlayerState("P2", PlayerType.HUMAN);

        match.addPlayer(p1);
        match.addPlayer(p2);

        require(!match.isGameOver(), "Fresh versus match should not be over");

        p2.status.alive = false;
        require(match.isGameOver(), "Versus match should end when one player remains");
        require(match.getLastAlivePlayer() == p1, "Remaining player should be reported as winner");
    }

    private static void verifyAiProducesPlannedInput() {
        Player aiPlayer = new Player(UUID.randomUUID(), PlayerType.AI, "AI1");
        PlayerState playerState = new PlayerState(aiPlayer, new ConfigState());
        playerState.piece.currentPiece = new Piece(PieceType.T, 0, 4, 0);
        playerState.next.next.add(PieceType.I);
        playerState.next.next.add(PieceType.O);

        MatchState match = new MatchState();
        match.addPlayer(playerState);

        HeuristicAI ai = new HeuristicAI(aiPlayer);
        PlayerInput input = ai.decide(aiPlayer, match);

        require(input.inputs != null && !input.inputs.isEmpty(), "AI did not produce a planned input");
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
        return createPlayerState("P1", PlayerType.HUMAN);
    }

    private static PlayerState createPlayerState(String name, PlayerType type) {
        Player player = new Player(UUID.randomUUID(), type, name);
        return new PlayerState(player, new ConfigState());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
