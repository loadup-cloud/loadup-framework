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
import com.github.loadup.components.database.sequence.SequenceRange;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceRepository {
    private static final Long minValue = 0L;
    private static final Long maxValue = Long.MAX_VALUE;
    private static final Long step = 1000L;
    private final JdbcAggregateTemplate template;

    public SequenceRepository(JdbcAggregateTemplate template) {
        this.template = template;
    }

    public SequenceRange getNextRange(String name) {
        Query query = Query.query(Criteria.where("name").is(name));
        Sequence sequence = template.findOne(query, Sequence.class).orElseGet(() -> {
            Sequence newSequence = new Sequence();
            newSequence.setMinValue(minValue);
            newSequence.setMaxValue(maxValue);
            newSequence.setStep(step);
            newSequence.setName(name);
            template.save(newSequence);
            return newSequence;
        });
        Long oldValue = sequence.getValue();
        if (oldValue < 0 || oldValue > maxValue || oldValue < minValue) {
            throw new RuntimeException("Sequence Value Error!");
        }
        Long beginValue = calculateBeginValue(name, oldValue);
        Long endValue = beginValue + step;
        if (endValue > maxValue) {
            endValue = maxValue;
        } else {
            endValue = endValue - 1;
        }
        if (beginValue > endValue) {
            throw new RuntimeException("Sequence Value Error!");
        }
        return new SequenceRange(beginValue, endValue);
    }

    private Long calculateBeginValue(String name, Long oldValue) {
        return 0L;
    }
}
