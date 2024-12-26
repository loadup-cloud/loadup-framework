package com.github.loadup.components.database.repository;

import com.github.loadup.components.database.Sequence;
import com.github.loadup.components.database.sequence.SequenceRange;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceRepository {
    private final JdbcAggregateTemplate template;

    private static final Long minValue = 0L;
    private static final Long maxValue = Long.MAX_VALUE;
    private static final Long step     = 1000L;

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
