/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.database.repository;

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

import com.github.loadup.components.database.Sequence;
import com.github.loadup.components.database.config.DatabaseProperties;
import com.github.loadup.components.database.sequence.SequenceRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceRepository {
    private static final Logger log = LoggerFactory.getLogger(SequenceRepository.class);

    private final Long minValue;
    private final Long maxValue;
    private final Long step;
    private final JdbcAggregateTemplate template;

    public SequenceRepository(JdbcAggregateTemplate template, DatabaseProperties properties) {
        this.template = template;
        this.minValue = properties.getSequence().getMinValue();
        this.maxValue = properties.getSequence().getMaxValue();
        this.step = properties.getSequence().getStep();
    }

    public synchronized SequenceRange getNextRange(String name) {
        Query query = Query.query(Criteria.where("name").is(name));
        Sequence sequence = template.findOne(query, Sequence.class).orElseGet(() -> {
            Sequence newSequence = new Sequence();
            newSequence.setMinValue(minValue);
            newSequence.setMaxValue(maxValue);
            newSequence.setStep(step);
            newSequence.setName(name);
            newSequence.setValue(minValue);
            return template.insert(newSequence);
        });

        Long oldValue = sequence.getValue() != null ? sequence.getValue() : minValue;
        if (oldValue < minValue || oldValue > maxValue) {
            throw new IllegalStateException(
                String.format("Sequence '%s' value %d is out of range [%d, %d]",
                    name, oldValue, minValue, maxValue));
        }

        Long endValue = Math.min(oldValue + step - 1, maxValue);

        if (oldValue > endValue) {
            throw new IllegalStateException(
                String.format("Sequence '%s' has been exhausted. Begin: %d, End: %d",
                    name, oldValue, endValue));
        }

        // Update the sequence value for next range
        sequence.setValue(endValue + 1);
        template.update(sequence);

        log.debug("Allocated sequence range for '{}': [{}, {}]", name, oldValue, endValue);
        return new SequenceRange(oldValue, endValue);
    }
}
