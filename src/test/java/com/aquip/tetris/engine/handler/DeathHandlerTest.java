package com.aquip.tetris.engine.handler;

import com.aquip.tetris.engine.TickContext;
import com.aquip.tetris.piece.Piece;
import com.aquip.tetris.piece.PieceType;
import com.aquip.tetris.player.Player;
import com.aquip.tetris.player.PlayerType;
import com.aquip.tetris.state.MatchState;
import com.aquip.tetris.state.PlayerState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeathHandlerTest {

    @Test
    void spawnCollisionKillsPlayer() {
        PlayerState player = createPlayerState();
        MatchState match = new MatchState();
        match.addPlayer(player);

        player.piece.currentPiece = new Piece(PieceType.O, 0, 4, 0);
        player.board.set(4, 0, 1);

        TickContext context = new TickContext();
        context.reset(List.of(player));
        context.get(player).pieceSpawned = true;

        new DeathHandler().apply(match, context);

        assertFalse(player.status.alive);
        assertNull(player.piece.currentPiece);
    }

    @Test
    void lockOutInHiddenRowsKillsPlayer() {
        PlayerState player = createPlayerState();
        MatchState match = new MatchState();
        match.addPlayer(player);

        player.board.set(0, 0, 1);

        TickContext context = new TickContext();
        context.reset(List.of(player));
        context.get(player).piecePlaced = true;

        new DeathHandler().apply(match, context);

        assertFalse(player.status.alive);
    }

    private PlayerState createPlayerState() {
        Player player = new Player(UUID.randomUUID(), PlayerType.HUMAN, "P1");
        return new PlayerState(player, new com.aquip.tetris.state.ConfigState());
    }
}
