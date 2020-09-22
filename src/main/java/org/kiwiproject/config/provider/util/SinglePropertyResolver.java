package org.kiwiproject.config.provider.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
import java.util.function.Function;

@UtilityClass
public class SinglePropertyResolver {

    private static final String RESOLUTION_VALUE_KEY = "value";
    private static final String RESOLUTION_METHOD_KEY = "method";

    /**
     * Resolves a {@link String} configuration property, defaulting the value to null
     *
     * @see SinglePropertyResolver#resolveProperty(ExternalConfigProvider, KiwiEnvironment, FieldResolverStrategy, String, String, String, Object, Function)
     */
    public static Pair<String, ResolvedBy> resolveProperty(ExternalConfigProvider externalConfigProvider,
                                                           KiwiEnvironment kiwiEnvironment,
                                                           FieldResolverStrategy<String> resolverStrategy,
                                                           String systemProperty,
                                                           String environmentVariable,
                                                           String externalKey) {

        return resolveProperty(externalConfigProvider, kiwiEnvironment, resolverStrategy, systemProperty,
                environmentVariable, externalKey, null, value -> value);
    }

    /**
     * Resolves a {@link String} configuration property
     *
     * @see SinglePropertyResolver#resolveProperty(ExternalConfigProvider, KiwiEnvironment, FieldResolverStrategy, String, String, String, Object, Function)
     */
    public static Pair<String, ResolvedBy> resolveProperty(ExternalConfigProvider externalConfigProvider,
                                                                KiwiEnvironment kiwiEnvironment,
                                                                FieldResolverStrategy<String> resolverStrategy,
                                                                String systemProperty,
                                                                String environmentVariable,
                                                                String externalKey,
                                                                String defaultValue) {

        return resolveProperty(externalConfigProvider, kiwiEnvironment, resolverStrategy, systemProperty,
                environmentVariable, externalKey, defaultValue, value -> value);
    }

    /**
     * Resolves a configuration property in the following order:
     *
     * <ol>
     *     <li>System property with the given system property key</li>
     *     <li>System property with the default system property key</li>
     *     <li>Environment variable with the given variable name</li>
     *     <li>Environment variable with the default variable name</li>
     *     <li>The value from an external configuration file with the given key</li>
     *     <li>The value from an external configuration file with the default key</li>
     *     <li>The value from a given supplier</li>
     *     <li>The value explicitly given</li>
     * </ol>
     *
     * @param externalConfigProvider    The provider that looks up config values in an external file
     * @param kiwiEnvironment           The kiwi environment to lookup environment variables
     * @param resolverStrategy          The field resolver strategy that contains the information on how to resolve a property
     * @param systemProperty            The default system property if not provided
     * @param environmentVariable       The default environment variable if not provided
     * @param externalKey               The default external file key if not provided
     * @param defaultValue              The default value to return if no other resolutions work
     * @param convertFromString         A Function that takes a string value and converts it to T
     * @param <T>                       The type of the value to be returned
     * @return                          The resolved value
     */
    @SuppressWarnings({"unchecked", "java:S107"})
    public static <T> Pair<T, ResolvedBy> resolveProperty(ExternalConfigProvider externalConfigProvider,
                                                                KiwiEnvironment kiwiEnvironment,
                                                                FieldResolverStrategy<T> resolverStrategy,
                                                                String systemProperty,
                                                                String environmentVariable,
                                                                String externalKey,
                                                                T defaultValue,
                                                                Function<String, T> convertFromString) {

        var nonNullResolver = isNull(resolverStrategy)
                ? FieldResolverStrategy.<T>builder().build() : resolverStrategy;

        var fromSystemProperty = System.getProperty(
                nonNullResolver.getSystemPropertyKeyOrDefault(systemProperty));

        var resolvedEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var fromEnvironment = resolvedEnvironment.getenv(nonNullResolver.getEnvVariableOrDefault(environmentVariable));

        if (isNotBlank(fromSystemProperty)) {
            return Pair.of(convertFromString.apply(fromSystemProperty), ResolvedBy.SYSTEM_PROPERTY);
        } else if (isNotBlank(fromEnvironment)) {
            return Pair.of(convertFromString.apply(fromEnvironment), ResolvedBy.SYSTEM_ENV);
        } else if (nonNull(nonNullResolver.getExplicitValue())) {
            return Pair.of(nonNullResolver.getExplicitValue(), ResolvedBy.EXPLICIT_VALUE);
        } else {
            var returnVal = new HashMap<String, Object>();
            getExternalPropertyProviderOrDefault(externalConfigProvider)
                    .usePropertyIfPresent(nonNullResolver.getExternalPropertyOrDefault(externalKey),
                            value -> {
                                returnVal.put(RESOLUTION_VALUE_KEY, convertFromString.apply(value));
                                returnVal.put(RESOLUTION_METHOD_KEY, ResolvedBy.EXTERNAL_PROPERTY);
                            },
                            () -> {
                                var value = nonNullResolver.getValueSupplierOrDefault(defaultValue).get();
                                returnVal.put(RESOLUTION_VALUE_KEY, value);
                                returnVal.put(RESOLUTION_METHOD_KEY, isNull(value) ? ResolvedBy.NONE : ResolvedBy.DEFAULT);
                            });

            return Pair.of((T) returnVal.get(RESOLUTION_VALUE_KEY), (ResolvedBy) returnVal.get(RESOLUTION_METHOD_KEY));
        }
    }
}
