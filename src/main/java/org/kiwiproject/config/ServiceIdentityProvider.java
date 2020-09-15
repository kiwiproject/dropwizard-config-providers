package org.kiwiproject.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Property provider that determines the identity of the service that is running.  Identity is defined by the service
 * name, service version, and deployment environment.
 * <p>
 * The provider will look for the identity fields in the following order:
 * <ol>
 *     <li>System property with the given system property key</li>
 *     <li>System property with the default system property key (kiwi.service.name, kiwi.service.version, kiwi.service.env)</li>
 *     <li>Environment variable with the given variable name</li>
 *     <li>Environment variable with the default variable name (KIWI_SERVICE_NAME, KIWI_SERVICE_VERSION, KIWI_SERVICE_ENV)</li>
 *     <li>The given network</li>
 *     <li>The named network from an external configuration file with the given key</li>
 *     <li>The named network from an external configuration file with the default key (service.name, service.version, service.env)</li>
 *     <li>The named network from a given supplier</li>
 * </ol>
 */
@Slf4j
public class ServiceIdentityProvider implements ConfigProvider {

    private static final String RESOLUTION_VALUE_KEY = "value";
    private static final String RESOLUTION_METHOD_KEY = "method";

    @VisibleForTesting
    static final String DEFAULT_NAME_SYSTEM_PROPERTY = "kiwi.service.name";

    @VisibleForTesting
    static final String DEFAULT_NAME_ENV_VARIABLE = "KIWI_SERVICE_NAME";

    @VisibleForTesting
    static final String DEFAULT_NAME_EXTERNAL_PROPERTY_KEY = "service.name";

    @VisibleForTesting
    static final String DEFAULT_VERSION_SYSTEM_PROPERTY = "kiwi.service.version";

    @VisibleForTesting
    static final String DEFAULT_VERSION_ENV_VARIABLE = "KIWI_SERVICE_VERSION";

    @VisibleForTesting
    static final String DEFAULT_VERSION_EXTERNAL_PROPERTY_KEY = "service.version";

    @VisibleForTesting
    static final String DEFAULT_ENVIRONMENT_SYSTEM_PROPERTY = "kiwi.service.env";

    @VisibleForTesting
    static final String DEFAULT_ENVIRONMENT_ENV_VARIABLE = "KIWI_SERVICE_ENV";

    @VisibleForTesting
    static final String DEFAULT_ENVIRONMENT_EXTERNAL_PROPERTY_KEY = "service.env";

    @Getter
    private String name;

    private ResolvedBy nameResolvedBy;

    @Getter
    private String version;

    private ResolvedBy versionResolvedBy;

    @Getter
    private String environment;

    private ResolvedBy environmentResolvedBy;

    @Builder
    private ServiceIdentityProvider(ExternalPropertyProvider externalPropertyProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    FieldResolverStrategy<String> nameResolverStrategy,
                                    FieldResolverStrategy<String> versionResolverStrategy,
                                    FieldResolverStrategy<String> environmentResolverStrategy) {

        var resolvedKiwiEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var extProvider = getExternalPropertyProviderOrDefault(externalPropertyProvider);

        resolveName(getResolverOrDefault(nameResolverStrategy), extProvider, resolvedKiwiEnvironment);
        resolveVersion(getResolverOrDefault(versionResolverStrategy), extProvider, resolvedKiwiEnvironment);
        resolveEnvironment(getResolverOrDefault(environmentResolverStrategy), extProvider, resolvedKiwiEnvironment);
    }

    private FieldResolverStrategy<String> getResolverOrDefault(FieldResolverStrategy<String> providedStrategy) {
        return isNull(providedStrategy) ? FieldResolverStrategy.<String>builder().build() : providedStrategy;
    }

    private void resolveName(FieldResolverStrategy<String> resolverStrategy,
                             ExternalPropertyProvider externalPropertyProvider,
                             KiwiEnvironment kiwiEnvironment) {

        var nameResolution = resolveField(resolverStrategy.getSystemPropertyKeyOrDefault(DEFAULT_NAME_SYSTEM_PROPERTY),
                resolverStrategy.getEnvVariableOrDefault(DEFAULT_NAME_ENV_VARIABLE),
                resolverStrategy.getExplicitValue(), resolverStrategy.getExternalPropertyOrDefault(DEFAULT_NAME_EXTERNAL_PROPERTY_KEY),
                resolverStrategy.getValueSupplierOrDefault(""), externalPropertyProvider, kiwiEnvironment);

        this.name = nameResolution.getLeft();
        this.nameResolvedBy = nameResolution.getRight();
    }

    private void resolveVersion(FieldResolverStrategy<String> resolverStrategy,
                             ExternalPropertyProvider externalPropertyProvider,
                             KiwiEnvironment kiwiEnvironment) {

        var versionResolution = resolveField(resolverStrategy.getSystemPropertyKeyOrDefault(DEFAULT_VERSION_SYSTEM_PROPERTY),
                resolverStrategy.getEnvVariableOrDefault(DEFAULT_VERSION_ENV_VARIABLE),
                resolverStrategy.getExplicitValue(), resolverStrategy.getExternalPropertyOrDefault(DEFAULT_VERSION_EXTERNAL_PROPERTY_KEY),
                resolverStrategy.getValueSupplierOrDefault(""), externalPropertyProvider, kiwiEnvironment);

        this.version = versionResolution.getLeft();
        this.versionResolvedBy = versionResolution.getRight();
    }

    private void resolveEnvironment(FieldResolverStrategy<String> resolverStrategy,
                                ExternalPropertyProvider externalPropertyProvider,
                                KiwiEnvironment kiwiEnvironment) {

        var envResolution = resolveField(resolverStrategy.getSystemPropertyKeyOrDefault(DEFAULT_ENVIRONMENT_SYSTEM_PROPERTY),
                resolverStrategy.getEnvVariableOrDefault(DEFAULT_ENVIRONMENT_ENV_VARIABLE),
                resolverStrategy.getExplicitValue(), resolverStrategy.getExternalPropertyOrDefault(DEFAULT_ENVIRONMENT_EXTERNAL_PROPERTY_KEY),
                resolverStrategy.getValueSupplierOrDefault(""), externalPropertyProvider, kiwiEnvironment);

        this.environment = envResolution.getLeft();
        this.environmentResolvedBy = envResolution.getRight();
    }

    private Pair<String, ResolvedBy> resolveField(String systemPropertyKey,
                                                  String envVariable,
                                                  String explicit,
                                                  String externalPropertyKey,
                                                  Supplier<String> supplier,
                                                  ExternalPropertyProvider externalPropertyProvider,
                                                  KiwiEnvironment kiwiEnvironment) {

        var fromSystemProperties = System.getProperty(systemPropertyKey);
        var fromEnv = kiwiEnvironment.getenv(envVariable);

        if (isNotBlank(fromSystemProperties)) {
            return Pair.of(fromSystemProperties, ResolvedBy.SYSTEM_PROPERTY);
        } else if (isNotBlank(fromEnv)) {
            return Pair.of(fromEnv, ResolvedBy.SYSTEM_ENV);
        } else if (nonNull(explicit)) {
            return Pair.of(explicit, ResolvedBy.EXPLICIT_VALUE);
        }

        var returnVal = new HashMap<String, Object>();
        externalPropertyProvider.usePropertyIfPresent(externalPropertyKey,
                value -> {
                    returnVal.put(RESOLUTION_VALUE_KEY, value);
                    returnVal.put(RESOLUTION_METHOD_KEY, ResolvedBy.EXTERNAL_PROPERTY);
                },
                () -> {
                    var value = supplier.get();
                    returnVal.put(RESOLUTION_VALUE_KEY, value);
                    returnVal.put(RESOLUTION_METHOD_KEY, isBlank(value) ? ResolvedBy.NONE : ResolvedBy.DEFAULT);
                });

        return Pair.of((String) returnVal.get(RESOLUTION_VALUE_KEY), (ResolvedBy) returnVal.get(RESOLUTION_METHOD_KEY));

    }

    private ExternalPropertyProvider getExternalPropertyProviderOrDefault(ExternalPropertyProvider providedProvider) {
        return nonNull(providedProvider) ? providedProvider : ExternalPropertyProvider.builder().build();
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(name) && isNotBlank(version) && isNotBlank(environment);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of(
                "name", nameResolvedBy,
                "version", versionResolvedBy,
                "environment", environmentResolvedBy
        );
    }
}
