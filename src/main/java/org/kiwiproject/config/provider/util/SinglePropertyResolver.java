package org.kiwiproject.config.provider.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.config.provider.ExternalConfigProvider.getExternalPropertyProviderOrDefault;

import lombok.experimental.UtilityClass;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.ExternalConfigProvider;
import org.kiwiproject.config.provider.FieldResolverStrategy;
import org.kiwiproject.config.provider.ResolvedBy;
import org.kiwiproject.config.provider.ResolverResult;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class SinglePropertyResolver {

    /**
     * Resolves a {@link String} configuration property, defaulting the value to null
     *
     * @see SinglePropertyResolver#resolveProperty(ExternalConfigProvider, KiwiEnvironment, FieldResolverStrategy, String, String, String, Object, Function)
     */
    public static ResolverResult<String> resolveProperty(ExternalConfigProvider externalConfigProvider,
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
    public static ResolverResult<String> resolveProperty(ExternalConfigProvider externalConfigProvider,
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
     * @param externalConfigProvider The provider that looks up config values in an external file
     * @param kiwiEnvironment        The kiwi environment to lookup environment variables
     * @param resolverStrategy       The field resolver strategy that contains the information on how to resolve a property
     * @param systemProperty         The default system property if not provided
     * @param environmentVariable    The default environment variable if not provided
     * @param externalKey            The default external file key if not provided
     * @param defaultValue           The default value to return if no other resolutions work
     * @param convertFromString      A Function that takes a string value and converts it to T
     * @param <T>                    The type of the value to be returned
     * @return The resolved value
     */
    @SuppressWarnings({"java:S107"})
    public static <T> ResolverResult<T> resolveProperty(ExternalConfigProvider externalConfigProvider,
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
            return new ResolverResult<>(convertFromString.apply(fromSystemProperty), ResolvedBy.SYSTEM_PROPERTY);
        } else if (isNotBlank(fromEnvironment)) {
            return new ResolverResult<>(convertFromString.apply(fromEnvironment), ResolvedBy.SYSTEM_ENV);
        }

        return getExternalPropertyProviderOrDefault(externalConfigProvider)
                .resolveExternalProperty(nonNullResolver.getExternalPropertyOrDefault(externalKey),
                        value -> new ResolverResult<>(convertFromString.apply(value), ResolvedBy.EXTERNAL_PROPERTY),
                        () -> resolveFromDefaults(nonNullResolver, defaultValue));
    }

    private <T> ResolverResult<T> resolveFromDefaults(FieldResolverStrategy<T> resolver, T defaultValue) {
        var supplierValue = Optional.ofNullable(resolver.getValueSupplier()).map(Supplier::get).orElse(null);
        if (nonNull(supplierValue)) {
            return new ResolverResult<>(supplierValue, ResolvedBy.SUPPLIER);
        }

        if (nonNull(resolver.getExplicitValue())) {
            return new ResolverResult<>(resolver.getExplicitValue(), ResolvedBy.EXPLICIT_VALUE);
        }

        if (nonNull(defaultValue)) {
            return new ResolverResult<>(defaultValue, ResolvedBy.PROVIDER_DEFAULT);
        }

        return new ResolverResult<>(null, ResolvedBy.NONE);
    }

}
