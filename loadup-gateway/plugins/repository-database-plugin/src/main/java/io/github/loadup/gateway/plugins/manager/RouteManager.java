package io.github.loadup.gateway.plugins.manager;

import io.github.loadup.gateway.plugins.entity.RouteEntity;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteManager extends CrudRepository<RouteEntity, String> {

    Optional<RouteEntity> findByPathAndMethod(String path, String method);
}
