package Client.circuitBreaker;

import Server.rateLimit.RateLimit;
import Server.rateLimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerProvider {
    private Map<String, CircuitBreaker> circuitBreakerMap = new HashMap<>();

    public synchronized CircuitBreaker getCircuitBreaker(String interfaceName){
        // 检查是否存在对应的熔断器
        if(!circuitBreakerMap.containsKey(interfaceName)){
            System.out.println("serviceName="+interfaceName+"创建一个新的熔断器");
            CircuitBreaker circuitBreaker = new CircuitBreaker(1,0.7,10000);
            circuitBreakerMap.put(interfaceName,circuitBreaker);
        }
        return circuitBreakerMap.get(interfaceName);
    }
}

