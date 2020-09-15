package org.kiwiproject.config;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.Builder;
import lombok.Getter;

import java.util.function.Supplier;

@Builder
public class FieldResolverStrategy<T> {

    @Getter
    private final T explicitValue;

    private final String systemPropertyKey;
    private final String envVariable;
    private final String externalProperty;
    private final Supplier<T> valueSupplier;

    public String getSystemPropertyKeyOrDefault(String defaultKey) {
        return isBlank(systemPropertyKey) ? defaultKey : systemPropertyKey;
    }

    public String getEnvVariableOrDefault(String defaultVariable) {
        return isBlank(envVariable) ? defaultVariable : envVariable;
    }

    public String getExternalPropertyOrDefault(String defaultProperty) {
        return isBlank(externalProperty) ? defaultProperty : externalProperty;
    }

    public Supplier<T> getValueSupplierOrDefault(T defaultValue) {
        if (isNull(valueSupplier)) {
            return () -> defaultValue;
        }

        return valueSupplier;
    }
}
