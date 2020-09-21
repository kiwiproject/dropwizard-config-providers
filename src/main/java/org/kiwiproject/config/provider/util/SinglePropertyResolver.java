package org.kiwiproject.config.provider.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.config.provider.ExternalConfigProvider.getExternalPropertyProviderOrDefault;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.ExternalConfigProvider;
import org.kiwiproject.config.provider.FieldResolverStrategy;
import org.kiwiproject.config.provider.ResolvedBy;

import java.util.HashMap;

@UtilityClass
public class SinglePropertyResolver {

    private static final String RESOLUTION_VALUE_KEY = "value";
    private static final String RESOLUTION_METHOD_KEY = "method";

    public static Pair<String, ResolvedBy> resolveStringProperty(ExternalConfigProvider externalConfigProvider,
                                                                 KiwiEnvironment kiwiEnvironment,
                                                                 FieldResolverStrategy<String> resolverStrategy,
                                                                 String systemProperty,
                                                                 String environmentVariable,
                                                                 String externalKey,
                                                                 String defaultValue) {

        var nonNullResolver = isNull(resolverStrategy)
                ? FieldResolverStrategy.<String>builder().build() : resolverStrategy;

        var fromSystemProperty = System.getProperty(
                nonNullResolver.getSystemPropertyKeyOrDefault(systemProperty));

        var resolvedEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var fromEnvironment = resolvedEnvironment.getenv(nonNullResolver.getEnvVariableOrDefault(environmentVariable));

        if (isNotBlank(fromSystemProperty)) {
            return Pair.of(fromSystemProperty, ResolvedBy.SYSTEM_PROPERTY);
        } else if (isNotBlank(fromEnvironment)) {
            return Pair.of(fromEnvironment, ResolvedBy.SYSTEM_ENV);
        } else if (nonNull(nonNullResolver.getExplicitValue())) {
            return Pair.of(nonNullResolver.getExplicitValue(), ResolvedBy.EXPLICIT_VALUE);
        } else {
            var returnVal = new HashMap<String, Object>();
            getExternalPropertyProviderOrDefault(externalConfigProvider)
                    .usePropertyIfPresent(nonNullResolver.getExternalPropertyOrDefault(externalKey),
                            value -> {
                                returnVal.put(RESOLUTION_VALUE_KEY, value);
                                returnVal.put(RESOLUTION_METHOD_KEY, ResolvedBy.EXTERNAL_PROPERTY);
                            },
                            () -> {
                                var value = nonNullResolver.getValueSupplierOrDefault(defaultValue).get();
                                returnVal.put(RESOLUTION_VALUE_KEY, value);
                                returnVal.put(RESOLUTION_METHOD_KEY, isBlank(value) ? ResolvedBy.NONE : ResolvedBy.DEFAULT);
                            });

            return Pair.of((String) returnVal.get(RESOLUTION_VALUE_KEY), (ResolvedBy) returnVal.get(RESOLUTION_METHOD_KEY));
        }
    }
}
