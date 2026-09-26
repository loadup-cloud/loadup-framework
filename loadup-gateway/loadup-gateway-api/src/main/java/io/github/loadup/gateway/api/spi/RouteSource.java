package io.github.loadup.gateway.api.spi;

import io.github.loadup.gateway.api.model.RouteDocument;

/** Provides a complete route snapshot; implementations may signal changes with RouteSourceChangedEvent. */
public interface RouteSource {
    RouteDocument loadCurrent();
}
