package io.github.loadup.commons.mapping;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct configuration for LoadUp converters.
 *
 * <p>Converters are generated as Spring beans ({@code componentModel = "spring"}) and
 * must map every target property explicitly. Unknown source properties only warn so a
 * future entity extension does not break unrelated mappings.
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.WARN)
public interface LoadUpMapStructConfig {}
