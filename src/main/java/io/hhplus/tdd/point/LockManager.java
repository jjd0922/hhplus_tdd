package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    public ReentrantLock getUserLock(Long userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }
}
