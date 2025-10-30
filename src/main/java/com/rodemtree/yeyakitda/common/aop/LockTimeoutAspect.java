package com.rodemtree.yeyakitda.common.aop;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LockTimeoutAspect {

    private final EntityManager entityManager;

    @Around("@annotation(lockTimeout)")
    public Object setLockTimeout(ProceedingJoinPoint joinPoint, LockTimeout lockTimeout) throws Throwable {
        int timeout = lockTimeout.timeoutSeconds();

        Object result = entityManager.createNativeQuery("SELECT @@innodb_lock_wait_timeout").getSingleResult();
        int originalTimeout = ((Number) result).intValue();

        entityManager.createNativeQuery("SET SESSION innodb_lock_wait_timeout = " + timeout)
                .executeUpdate();

        try {
            return joinPoint.proceed();
        } finally {
            entityManager.createNativeQuery("SET SESSION innodb_lock_wait_timeout = " + originalTimeout)
                    .executeUpdate();
        }

    }
}
