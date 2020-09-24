package org.kiwiproject.config.provider.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.config.provider.ExternalConfigProvider.getExternalPropertyProviderOrDefault;

import lombok.experimental.UtilityClass;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.config.provider.FieldResolverStrategy;
import org.kiwiproject.config.provider.ResolvedBy;
import org.kiwiproject.config.provider.ResolverResult;

import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass
public class SinglePropertyResolver {

    public static ResolverResult<String> resolveStringProperty(PropertyResolutionSettings<String> settings) {
        return resolveProperty(settings.toBuilder().convertFromString(value -> value).build());
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
     * @param settings  A set of settings to figure out the resolution process
     * @param <T>       The type of the value to be returned
     * @return The resolved value
     */
    public static <T> ResolverResult<T> resolveProperty(PropertyResolutionSettings<T> settings) {

        var nonNullResolver = isNull(settings.getResolverStrategy())
                ? FieldResolverStrategy.<T>builder().build() : settings.getResolverStrategy();

        var fromSystemProperty = System.getProperty(
                nonNullResolver.getSystemPropertyKeyOrDefault(settings.getSystemProperty()));

        var resolvedEnvironment = isNull(settings.getKiwiEnvironment()) ? new DefaultEnvironment() : settings.getKiwiEnvironment();
        var fromEnvironment = resolvedEnvironment.getenv(nonNullResolver.getEnvVariableOrDefault(settings.getEnvironmentVariable()));

        if (isNotBlank(fromSystemProperty)) {
            return new ResolverResult<>(settings.getConvertFromString().apply(fromSystemProperty), ResolvedBy.SYSTEM_PROPERTY);
        } else if (isNotBlank(fromEnvironment)) {
            return new ResolverResult<>(settings.getConvertFromString().apply(fromEnvironment), ResolvedBy.SYSTEM_ENV);
        }

        return getExternalPropertyProviderOrDefault(settings.getExternalConfigProvider())
                .resolveExternalProperty(nonNullResolver.getExternalPropertyOrDefault(settings.getExternalKey()),
                        value -> new ResolverResult<>(settings.getConvertFromString().apply(value), ResolvedBy.EXTERNAL_PROPERTY),
                        () -> resolveFromDefaults(nonNullResolver, settings.getDefaultValue()));
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
