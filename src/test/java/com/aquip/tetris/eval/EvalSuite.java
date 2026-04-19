package com.aquip.tetris.eval;

import com.aquip.tetris.ai.AIConfig;
import com.aquip.tetris.state.ConfigState;
import com.aquip.tetris.state.MatchState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs a batch of EvalScenarios and prints a formatted summary.
 */
public class EvalSuite {

    private final EvalRunner runner;
    private final List<EvalScenario> scenarios = new ArrayList<>();

    public EvalSuite(ConfigState config, AIConfig aiConfig) {
        this.runner = new EvalRunner(config, aiConfig);
    }

    public EvalSuite add(EvalScenario scenario) {
        scenarios.add(scenario);
        return this;
    }

    public EvalSuite addAll(List<EvalScenario> list) {
        scenarios.addAll(list);
        return this;
    }

    // =========================================================================
    // Run
    // =========================================================================

    /**
     * Runs every scenario in order.
     *
     * @param renderCallback optional Swing hook; pass {@code null} to run
     *                       headlessly
     * @return results in scenario order
     */
    public List<EvalResult> runAll(Consumer<MatchState> renderCallback) {
        List<EvalResult> results = new ArrayList<>();

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║  EvalSuite — %d scenario(s)%n", scenarios.size());
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝\n");

        for (int i = 0; i < scenarios.size(); i++) {
            EvalScenario scenario = scenarios.get(i);
            System.out.printf("[%2d/%2d] Running: %s%n", i + 1, scenarios.size(), scenario.name);

            long start = System.currentTimeMillis();
            EvalResult r = runner.run(scenario, renderCallback);
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("       " + r);
            System.out.printf("       Wall time: %d ms%n%n", elapsed);

            results.add(r);
        }

        printSummary(results);
        return results;
    }

    // =========================================================================
    // Summary
    // =========================================================================

    private void printSummary(List<EvalResult> results) {
        long passed = results.stream().filter(r -> r.passed).count();
        long failed = results.size() - passed;
        int lines = results.stream().mapToInt(r -> r.totalLinesCleared).sum();
        double avgLPP = results.stream()
                .mapToDouble(r -> r.linesPerPiece)
                .average()
                .orElse(0);
        double avgAPP = results.stream()
                .mapToDouble(r -> r.attackPerPiece)
                .average()
                .orElse(0);

        System.out.println("════════════════════════════════════════════════════════════════════════════");
        System.out.printf("  SUMMARY  %d passed / %d failed  |  Total lines: %d  |  Avg L/P: %.2f  |  Avg A/P: %.2f%n",
                passed, failed, lines, avgLPP, avgAPP);
        System.out.println("════════════════════════════════════════════════════════════════════════════\n");

        // Per-termination breakdown
        long completed = results.stream()
                .filter(r -> r.termination == EvalResult.TerminationReason.COMPLETED).count();
        long gameOver = results.stream()
                .filter(r -> r.termination == EvalResult.TerminationReason.GAME_OVER).count();
        long timeout = results.stream()
                .filter(r -> r.termination == EvalResult.TerminationReason.TIMEOUT).count();

        System.out.printf("  Termination breakdown → Completed: %d  |  Game-over: %d  |  Timeout: %d%n%n",
                completed, gameOver, timeout);
    }
}