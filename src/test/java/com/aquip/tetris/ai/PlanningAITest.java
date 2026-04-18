// package com.aquip.tetris.ai;

// import com.aquip.tetris.ai.eval.EvaluationFunction;
// import com.aquip.tetris.ai.planner.InputPlanner;
// import com.aquip.tetris.ai.search.FutureStateGenerator;
// import com.aquip.tetris.input.GameInput;
// import com.aquip.tetris.input.PlayerInput;
// import com.aquip.tetris.piece.Piece;
// import com.aquip.tetris.piece.PieceType;
// import com.aquip.tetris.player.Player;
// import com.aquip.tetris.player.PlayerType;
// import com.aquip.tetris.state.ConfigState;
// import com.aquip.tetris.state.MatchState;
// import com.aquip.tetris.state.PlayerState;
// import org.junit.jupiter.api.Test;

// import java.util.ArrayDeque;
// import java.util.List;
// import java.util.Queue;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// class PlanningAITest {

// @Test
// void batchesFinalRotationWithHardDrop() {
// Player player = new Player(UUID.randomUUID(), PlayerType.AI, "AI1");
// PlayerState playerState = new PlayerState(player, new ConfigState());
// playerState.piece.currentPiece = new Piece(PieceType.T, 0, 4, 0);
// playerState.next.next.add(PieceType.I);

// MatchState match = new MatchState();
// match.addPlayer(playerState);

// FutureStateGenerator generator = current -> List.of(current);
// EvaluationFunction evaluator = state -> 1.0;
// InputPlanner planner = (from, to) -> {
// Queue<GameInput> queue = new ArrayDeque<>();
// queue.add(GameInput.ROTATE_CW);
// queue.add(GameInput.HARD_DROP);
// return queue;
// };

// PlanningAI ai = new PlanningAI(generator, evaluator, planner);
// PlayerInput input = ai.decide(player, match);

// assertEquals(2, input.inputs.size());
// assertTrue(input.inputs.contains(GameInput.ROTATE_CW));
// assertTrue(input.inputs.contains(GameInput.HARD_DROP));
// }
// }
