package com.github.loadup.components.database.service.impl;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.loadup.components.database.repository.SequenceRepository;
import com.github.loadup.components.database.sequence.SequenceRange;
import com.github.loadup.components.database.service.SequenceService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SequenceServiceImpl implements SequenceService {
    private static final Logger log  = LoggerFactory.getLogger(SequenceServiceImpl.class);
    private final        Lock   lock = new ReentrantLock();

    @Resource
    private SequenceRepository sequenceRepository;

    private volatile SequenceRange currentRange;

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
                        log.debug("Current sequence range exhausted for '{}', allocating new range", sequenceName);
                        currentRange = sequenceRepository.getNextRange(sequenceName);
                        log.debug("New sequence range allocated for '{}': {}", sequenceName, currentRange);
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
            throw new IllegalStateException(
                String.format("Sequence '%s' value overflow or exhausted. Current value: %d",
                    sequenceName, value));
        }

        return value;
    }

    @Override
    public LocalDateTime getSystemDate() {
        return LocalDateTime.now();
    }
}
