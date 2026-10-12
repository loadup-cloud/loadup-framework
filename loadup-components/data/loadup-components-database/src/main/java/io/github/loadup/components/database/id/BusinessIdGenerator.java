/*
 * #%L
 * Loadup Components Database
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.database.id;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.database.sequence.DatabaseResultCodeEnum;
import io.github.loadup.components.database.sequence.Sequence;
import io.github.loadup.components.database.sequence.SequenceScope;
import java.time.Clock;
import java.time.LocalDate;

/** Generates unique business IDs using a single captured local date per call. */
public final class BusinessIdGenerator {
    private final Sequence sequence;
    private final Clock clock;
    private final BusinessIdFormatter formatter;

    public BusinessIdGenerator(Sequence sequence, Clock clock, BusinessIdFormatter formatter) {
        this.sequence = sequence;
        this.clock = clock;
        this.formatter = formatter;
    }

    public String generate(BusinessIdCommand command) {
        LocalDate date = LocalDate.now(clock);
        SequenceScope scope = scope(command, date);
        return formatter.format(new BusinessIdParts(date, command, sequence.next(scope)));
    }

    public String[] generateBatch(BusinessIdCommand command, int count) {
        LocalDate date = LocalDate.now(clock);
        SequenceScope scope = scope(command, date);
        long[] numbers = sequence.nextBatch(scope, count);
        String[] ids = new String[numbers.length];
        for (int i = 0; i < ids.length; i++) ids[i] = formatter.format(new BusinessIdParts(date, command, numbers[i]));
        return ids;
    }

    private SequenceScope scope(BusinessIdCommand command, LocalDate date) {
        if (command == null || date.getYear() < 1 || date.getYear() > 9999)
            throw new CommonException(DatabaseResultCodeEnum.INVALID_INPUT);
        return new SequenceScope(
                "business-" + command.dataVersion() + command.systemVersion() + command.eventCode() + "-"
                        + command.siteId(),
                date,
                BusinessIdFormatter.MAX_SEQUENCE);
    }
}
