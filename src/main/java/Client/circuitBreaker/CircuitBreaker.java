package Client.circuitBreaker;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private AtomicInteger failureCount = new AtomicInteger(0);
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger requestCount = new AtomicInteger(0);

    private final int failureThreshold;
    private final double halfOpenSuccessRate;
    private final long retryTimePeriod;
    private long lastFailureTime = 0;

    private final int halfOpenMaxRequest = 5; // 半开状态允许的最多试探请求数

    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        switch (state) {
            case OPEN:
                if (currentTime - lastFailureTime > retryTimePeriod) {
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    return true;
                }
                return false;
            case HALF_OPEN:
                requestCount.incrementAndGet();  // 先计数
                if (requestCount.get() >= halfOpenMaxRequest) {
                    evaluateHalfOpenState();     // 试探完成，评估
                }
                return true;  // 本次请求依然放行
            case CLOSED:
            default:
                return true;
        }
    }

    // 出现成功请求
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            successCount.incrementAndGet();
            // 评估是否更改半开状态
            evaluateHalfOpenState();
        } else {
            resetCounts();
        }
    }

    // 出现失败请求
    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        // 若当前处于半开状态
        if (state == CircuitBreakerState.HALF_OPEN) {
            // 评估是否更改半开状态
            evaluateHalfOpenState();
        } else if (failureCount.get() >= failureThreshold) {
            state = CircuitBreakerState.OPEN;
        }
    }

    private void evaluateHalfOpenState() {
        int total = requestCount.get();
        int success = successCount.get();
        int failure = total - success;
        double successRate = total == 0 ? 0 : (double) success / total;

        if (total >= halfOpenMaxRequest) {
            if (successRate >= halfOpenSuccessRate) {
                state = CircuitBreakerState.CLOSED;
                resetCounts();
            } else {
                state = CircuitBreakerState.OPEN;
                lastFailureTime = System.currentTimeMillis();
                resetCounts();
            }
        }
    }

    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }

    public CircuitBreakerState getState() {
        return state;
    }
}

enum CircuitBreakerState {
    CLOSED, OPEN, HALF_OPEN
}
