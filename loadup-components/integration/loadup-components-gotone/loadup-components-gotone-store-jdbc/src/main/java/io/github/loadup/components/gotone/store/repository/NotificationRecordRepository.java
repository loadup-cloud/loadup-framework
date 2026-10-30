package io.github.loadup.components.gotone.store.repository;

import io.github.loadup.components.gotone.store.dataobject.NotificationRecordRow;
import org.springframework.data.repository.ListCrudRepository;

/** Spring Data JDBC repository for Gotone notification records. */
public interface NotificationRecordRepository extends ListCrudRepository<NotificationRecordRow, String> {}
