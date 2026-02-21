package com.quickticket.quickticket.shared.aspects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(1) // 1 < TransactionConfig.TRANSACTION_ORDER
@RequiredArgsConstructor
public class DistributedLockAspect {
    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object aroundDistributedLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
            throws Throwable {

        String lockKey = parseKey(joinPoint, distributedLock.key());
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        RLock rLock = redissonClient.getLock(lockKey);
        boolean lockAcquired = false;

        try {
            lockAcquired = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!lockAcquired) {
                throw new IllegalStateException("락 획득 실패 - lockKey: " + lockKey);
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (lockAcquired && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    @Around("@annotation(distributedWriteLock)")
    public Object aroundDistributedWriteLock(ProceedingJoinPoint joinPoint, DistributedWriteLock distributedWriteLock)
            throws Throwable {

        String lockKey = parseKey(joinPoint, distributedWriteLock.key());
        long waitTime = distributedWriteLock.waitTime();
        long leaseTime = distributedWriteLock.leaseTime();

        RLock writeLock = redissonClient.getReadWriteLock(lockKey).writeLock();
        boolean lockAcquired = false;

        try {
            lockAcquired = writeLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!lockAcquired) {
                throw new IllegalStateException("락 획득 실패 - lockKey: " + lockKey);
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (lockAcquired && writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
    }

    /**
      * SpEL 표현식을 기반으로 락 키를 생성
      *
      * @param joinPoint 메서드 실행 정보
      * @param keyExpression SpEL 표현식
      * @return 생성된 락 키
      */
    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
