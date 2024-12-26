package com.github.loadup.components.database.service;

import java.time.LocalDateTime;

public interface SequenceService {
    Long getNextSequence(String sequenceName);

    LocalDateTime getSystemDate();
}
