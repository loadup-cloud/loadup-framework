package com.github.loadup.components.database.repository;

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
    Sequence sequence =
        template
            .findOne(query, Sequence.class)
            .orElseGet(
                () -> {
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
          String.format(
              "Sequence '%s' value %d is out of range [%d, %d]",
              name, oldValue, minValue, maxValue));
    }

    Long endValue = Math.min(oldValue + step - 1, maxValue);

    if (oldValue > endValue) {
      throw new IllegalStateException(
          String.format(
              "Sequence '%s' has been exhausted. Begin: %d, End: %d", name, oldValue, endValue));
    }

    // Update the sequence value for next range
    sequence.setValue(endValue + 1);
    template.update(sequence);

    log.debug("Allocated sequence range for '{}': [{}, {}]", name, oldValue, endValue);
    return new SequenceRange(oldValue, endValue);
  }
}
