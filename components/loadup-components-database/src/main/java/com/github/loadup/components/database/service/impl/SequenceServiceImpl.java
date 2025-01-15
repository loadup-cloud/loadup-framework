package com.github.loadup.components.database.service.impl;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
