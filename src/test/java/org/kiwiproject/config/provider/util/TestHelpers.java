package org.kiwiproject.config.provider.util;

import static org.mockito.Mockito.when;

import lombok.experimental.UtilityClass;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.FieldResolverStrategy;

import java.util.function.Supplier;

@UtilityClass
public class TestHelpers {

    public static <T> FieldResolverStrategy<T> newSystemPropertyFieldResolverStrategy(String systemPropertyKey) {
        return FieldResolverStrategy.<T>builder().systemPropertyKey(systemPropertyKey).build();
    }

    public static <T> FieldResolverStrategy<T> newEnvVarFieldResolverStrategy(String envVariable) {
        return FieldResolverStrategy.<T>builder().envVariable(envVariable).build();
    }

    public static <T> FieldResolverStrategy<T> newExternalPropertyFieldResolverStrategy(String externalProperty) {
        return FieldResolverStrategy.<T>builder().externalProperty(externalProperty).build();
    }

    public static <T> FieldResolverStrategy<T> newExplicitValueFieldResolverStrategy(T explicitValue) {
        return FieldResolverStrategy.<T>builder().explicitValue(explicitValue).build();
    }

    public static <T> FieldResolverStrategy<T> newSupplierFieldResolverStrategy(Supplier<T> supplier) {
        return FieldResolverStrategy.<T>builder().valueSupplier(supplier).build();
    }

    public static void mockEnvToReturn(KiwiEnvironment env, String property, String valueToReturn) {
        when(env.getenv(property)).thenReturn(valueToReturn);
    }
}
