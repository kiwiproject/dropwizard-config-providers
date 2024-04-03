package org.kiwiproject.config.provider;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

/**
 * Factory class to easily create @link FieldResolverStrategy} instances
 * for specific use cases, for example when you only want a resolver
 * that uses a single resolution mechanism.
 */
@UtilityClass
public class FieldResolverStrategies {

    /**
     * Create a strategy that only resolves an environment variable.
     *
     * @param <T> the type to resolve
     * @param envVariable the name of the environment variable to resolve
     * @return a new instance
     */
    public static <T> FieldResolverStrategy<T> newEnvVarFieldResolverStrategy(String envVariable) {
        return FieldResolverStrategy.<T>builder().envVariable(envVariable).build();
    }

    /**
     * Create a strategy that only resolves an explicit value.
     *
     * @param <T> the type to resolve
     * @param explicitValue the explicit value to resolve
     * @return a new instance
     */
    public static <T> FieldResolverStrategy<T> newExplicitValueFieldResolverStrategy(T explicitValue) {
        return FieldResolverStrategy.<T>builder().explicitValue(explicitValue).build();
    }

    /**
     * Create a strategy that only resolves an external property.
     *
     * @param <T> the type to resolve
     * @param externalProperty the name of the external property to resolve
     * @return a new instance
     */
    public static <T> FieldResolverStrategy<T> newExternalPropertyFieldResolverStrategy(String externalProperty) {
        return FieldResolverStrategy.<T>builder().externalProperty(externalProperty).build();
    }

    /**
     * Create a strategy that only resolves a system property.
     *
     * @param <T> the type to resolve
     * @param systemPropertyKey the name of the system property to resolve
     * @return a new instance
     */
    public static <T> FieldResolverStrategy<T> newSystemPropertyFieldResolverStrategy(String systemPropertyKey) {
        return FieldResolverStrategy.<T>builder().systemPropertyKey(systemPropertyKey).build();
    }

    /**
     * Create a strategy that only resolves a Supplier.
     *
     * @param <T> the type to resolve
     * @param supplier the Supplier which will resolve the value
     * @return a new instance
     */
    public static <T> FieldResolverStrategy<T> newSupplierFieldResolverStrategy(Supplier<T> supplier) {
        return FieldResolverStrategy.<T>builder().valueSupplier(supplier).build();
    }
}
