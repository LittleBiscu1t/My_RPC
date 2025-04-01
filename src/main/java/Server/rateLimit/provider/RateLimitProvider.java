package Server.rateLimit.provider;

import Server.rateLimit.RateLimit;
import Server.rateLimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据接口名称获得对应的速率限制器实例
 * 如果接口的速率限制器不存在，则会创建一个新的实例并返回
 */
public class RateLimitProvider {
    private Map<String, RateLimit> rateLimitMap = new HashMap<>();

    public RateLimit getRateLimit(String interfaceName){
        if(!rateLimitMap.containsKey(interfaceName)){
            //使用基于令牌桶算法的速率限制器
            //每100ms一个令牌，最大容量为10
            RateLimit rateLimit=new TokenBucketRateLimitImpl(100,10);
            rateLimitMap.put(interfaceName,rateLimit);
            return rateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }
}
