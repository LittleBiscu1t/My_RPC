package Server.rateLimit.impl;

import Server.rateLimit.RateLimit;

public class TokenBucketRateLimitImpl implements RateLimit {
    //令牌产生速率（单位为ms）
    private static int RATE;
    //令牌桶容量
    private static int CAPACITY;
    //令牌桶当前令牌数量
    private volatile int curCapacity;
    //上次请求的时间戳
    private volatile long timeStamp;

    public TokenBucketRateLimitImpl(int rate, int capacity) {
        RATE = rate;
        CAPACITY = capacity;
        //当前容量初始化为最大容量。即一开始令牌桶是满的
        curCapacity = capacity;
    }

    @Override
    public boolean getToken() {
        //若桶内还有令牌，直接使用一个令牌
        if(curCapacity > 0){
            curCapacity--;
            return true;
        }
        //当前时间
        long current = System.currentTimeMillis();
        //若桶内令牌数为0， 开始计算生成令牌的情况
        //当前时间足够生成令牌，进行判断
        if(current - timeStamp > RATE){
            //若令牌数量>=2，则更新当前令牌数量
            if((current - timeStamp) / RATE >= 2){
                //-1是因为当前请求要消耗一个令牌
                curCapacity += (int) ((current - timeStamp) / RATE) - 1;
            }
            //保证令牌数量不大于桶容量
            if(curCapacity > CAPACITY){
                curCapacity = CAPACITY;
            }
            //更新时间戳
            timeStamp = current;
            return true;
        }
        //当前时间还不够生成令牌，返回失败
        return false;
    }
}
