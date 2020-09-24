package org.kiwiproject.config.provider.util;

import lombok.Builder;
import lombok.Getter;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.ExternalConfigProvider;
import org.kiwiproject.config.provider.FieldResolverStrategy;

import java.util.function.Function;

@Builder(toBuilder = true)
@Getter
public class PropertyResolutionSettings<T> {

    private final ExternalConfigProvider externalConfigProvider;
    private final KiwiEnvironment kiwiEnvironment;
    private final FieldResolverStrategy<T> resolverStrategy;
    private final String systemProperty;
    private final String environmentVariable;
    private final String externalKey;
    private final T defaultValue;
    private final Function<String, T> convertFromString;

}
