package ioioi.it.mltraining.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for parallel indicator calculation execution.
 *
 * Creates a dedicated ForkJoinPool for parallelizing candle indicator calculations
 * across all available CPU cores. This pool is isolated from the common ForkJoinPool
 * to prevent interference with other application parallelism.
 */
@Configuration
@Slf4j
public class ExecutorConfig {

    private ForkJoinPool indicatorCalculationPool;

    /**
     * Creates a dedicated ForkJoinPool for parallel indicator calculations.
     *
     * The pool size is configurable via application.yml (indicator.calculation.parallelism).
     * Default: Runtime.getRuntime().availableProcessors()
     * Recommendation: Use (processors - 2) in production to leave headroom for system tasks
     *
     * @param parallelism the desired parallelism level (number of worker threads)
     * @return configured ForkJoinPool instance
     */
    @Bean("indicatorCalculationPool")
    public ForkJoinPool indicatorCalculationPool(
            @Value("${indicator.calculation.parallelism:#{T(Runtime).getRuntime().availableProcessors()}}")
            int parallelism) {

        log.info("Creating ForkJoinPool for indicator calculations with parallelism level: {}", parallelism);
        log.info("Available processors: {}", Runtime.getRuntime().availableProcessors());

        this.indicatorCalculationPool = new ForkJoinPool(parallelism);

        return this.indicatorCalculationPool;
    }

    /**
     * Gracefully shuts down the indicator calculation pool on application shutdown.
     * Waits up to 60 seconds for running tasks to complete.
     */
    @PreDestroy
    public void shutdownPools() {
        if (indicatorCalculationPool != null && !indicatorCalculationPool.isShutdown()) {
            log.info("Shutting down indicator calculation ForkJoinPool...");

            indicatorCalculationPool.shutdown();

            try {
                if (!indicatorCalculationPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("ForkJoinPool did not terminate gracefully, forcing shutdown");
                    indicatorCalculationPool.shutdownNow();

                    if (!indicatorCalculationPool.awaitTermination(10, TimeUnit.SECONDS)) {
                        log.error("ForkJoinPool did not terminate after forced shutdown");
                    }
                }
                log.info("ForkJoinPool shutdown complete");
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for ForkJoinPool shutdown", e);
                indicatorCalculationPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
