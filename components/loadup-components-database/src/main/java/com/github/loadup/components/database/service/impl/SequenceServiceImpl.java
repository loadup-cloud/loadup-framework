package com.github.loadup.components.database.service.impl;

import com.github.loadup.components.database.repository.SequenceRepository;
import com.github.loadup.components.database.sequence.SequenceRange;
import com.github.loadup.components.database.service.SequenceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SequenceServiceImpl implements SequenceService {
    @Resource
    private          SequenceRepository sequenceRepository;
    private volatile SequenceRange      currentRange;
    private final    Lock               lock = new ReentrantLock();

    @Override
    public Long getNextSequence(String sequenceName) {
        if (currentRange == null) {
            lock.lock();
            try {
                if (currentRange == null) {
                    currentRange = sequenceRepository.getNextRange(sequenceName);
                }
            } finally {
                lock.unlock();
            }
        }
        long value = currentRange.getAndIncrement();
        if (value == -1) {
            lock.lock();
            try {
                for (; ; ) {
                    if (currentRange.isOver()) {
                        currentRange = sequenceRepository.getNextRange(sequenceName);
                    }
                    value = currentRange.getAndIncrement();
                    if (value == -1) {
                        continue;
                    }
                    break;
                }
            } finally {
                lock.unlock();
            }
        }
        if (value < 0) {
            throw new RuntimeException("Sequence value overflow, value = " + value);
        }

        return value;
    }

    @Override
    public LocalDateTime getSystemDate() {
        return LocalDateTime.now();
    }
}
