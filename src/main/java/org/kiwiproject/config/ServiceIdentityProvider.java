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
    static final String DEFAULT_NAME_ENV_VARIABLE = "KIW_SERVICE_NAME";

    @VisibleForTesting
    static final String DEFAULT_NAME_EXTERNAL_PROPERTY_KEY = "service.name";

    @VisibleForTesting
    static final String DEFAULT_VERSION_SYSTEM_PROPERTY = "kiwi.service.version";

    @VisibleForTesting
    static final String DEFAULT_VERSION_ENV_VARIABLE = "KIW_SERVICE_VERSION";

    @VisibleForTesting
    static final String DEFAULT_VERSION_EXTERNAL_PROPERTY_KEY = "service.version";

    @VisibleForTesting
    static final String DEFAULT_ENVIRONMENT_SYSTEM_PROPERTY = "kiwi.service.env";

    @VisibleForTesting
    static final String DEFAULT_ENVIRONMENT_ENV_VARIABLE = "KIW_SERVICE_ENV";

    @VisibleForTesting
    static final String DEFAULT_ENVIRONMENT_EXTERNAL_PROPERTY_KEY = "service.env";

    @Getter
    private final String name;

    private final ResolvedBy nameResolvedBy;

    @Getter
    private final String version;

    private final ResolvedBy versionResolvedBy;

    @Getter
    private final String environment;

    private final ResolvedBy environmentResolvedBy;

    @SuppressWarnings("java:S107")
    @Builder
    private ServiceIdentityProvider(String serviceName,
                                    String serviceVersion,
                                    String serviceEnvironment,
                                    String nameSystemPropertyKey,
                                    String nameEnvVariable,
                                    String nameExternalProperty,
                                    String versionSystemPropertyKey,
                                    String versionEnvVariable,
                                    String versionExternalProperty,
                                    String environmentSystemPropertyKey,
                                    String environmentEnvVariable,
                                    String environmentExternalProperty,
                                    ExternalPropertyProvider externalPropertyProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    Supplier<String> nameSupplier,
                                    Supplier<String> versionSupplier,
                                    Supplier<String> environmentSupplier) {

        var nameFromSystemProperties = System.getProperty(getSystemPropertyOrDefault(nameSystemPropertyKey, DEFAULT_NAME_SYSTEM_PROPERTY));
        var versionFromSystemProperties = System.getProperty(getSystemPropertyOrDefault(versionSystemPropertyKey, DEFAULT_VERSION_SYSTEM_PROPERTY));
        var environmentFromSystemProperties = System.getProperty(getSystemPropertyOrDefault(environmentSystemPropertyKey, DEFAULT_ENVIRONMENT_SYSTEM_PROPERTY));

        var resolvedKiwiEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var nameFromEnv = resolvedKiwiEnvironment.getenv(getEnvironmentVariableOrDefault(nameEnvVariable, DEFAULT_NAME_ENV_VARIABLE));
        var versionFromEnv = resolvedKiwiEnvironment.getenv(getEnvironmentVariableOrDefault(versionEnvVariable, DEFAULT_VERSION_ENV_VARIABLE));
        var environmentFromEnv = resolvedKiwiEnvironment.getenv(getEnvironmentVariableOrDefault(environmentEnvVariable, DEFAULT_ENVIRONMENT_ENV_VARIABLE));

        var extProvider = getExternalPropertyProviderOrDefault(externalPropertyProvider);

        var nameResolution = resolveField(nameFromSystemProperties, nameFromEnv, serviceName,
                extProvider, nameExternalProperty, DEFAULT_NAME_EXTERNAL_PROPERTY_KEY, nameSupplier);
        this.name = nameResolution.getLeft();
        this.nameResolvedBy = nameResolution.getRight();

        var versionResolution = resolveField(versionFromSystemProperties, versionFromEnv, serviceVersion,
                extProvider, versionExternalProperty, DEFAULT_VERSION_EXTERNAL_PROPERTY_KEY, versionSupplier);
        this.version = versionResolution.getLeft();
        this.versionResolvedBy = versionResolution.getRight();

        var environmentResolution = resolveField(environmentFromSystemProperties, environmentFromEnv,
                serviceEnvironment, extProvider, environmentExternalProperty, DEFAULT_ENVIRONMENT_EXTERNAL_PROPERTY_KEY,
                environmentSupplier);
        this.environment = environmentResolution.getLeft();
        this.environmentResolvedBy = environmentResolution.getRight();
    }

    private String getSystemPropertyOrDefault(String providedPropertyName, String defaultPropertyName) {
        return isBlank(providedPropertyName) ? defaultPropertyName : providedPropertyName;
    }

    private String getEnvironmentVariableOrDefault(String providedEnvironmentVariable, String defaultVariable) {
        return isBlank(providedEnvironmentVariable) ? defaultVariable : providedEnvironmentVariable;
    }

    private Pair<String, ResolvedBy> resolveField(String fromSystemProperties,
                                                  String fromEnv,
                                                  String explicit,
                                                  ExternalPropertyProvider externalPropertyProvider,
                                                  String externalPropertyKey,
                                                  String defaultExternalPropertyKey,
                                                  Supplier<String> supplier) {

        if (isNotBlank(fromSystemProperties)) {
            return Pair.of(fromSystemProperties, ResolvedBy.SYSTEM_PROPERTY);
        } else if (isNotBlank(fromEnv)) {
            return Pair.of(fromEnv, ResolvedBy.SYSTEM_ENV);
        } else if (nonNull(explicit)) {
            return Pair.of(explicit, ResolvedBy.EXPLICIT_VALUE);
        }

        var externalPropKey = isBlank(externalPropertyKey) ? defaultExternalPropertyKey : externalPropertyKey;

        var returnVal = new HashMap<String, Object>();
        externalPropertyProvider.usePropertyIfPresent(externalPropKey,
                value -> {
                    returnVal.put(RESOLUTION_VALUE_KEY, value);
                    returnVal.put(RESOLUTION_METHOD_KEY, ResolvedBy.EXTERNAL_PROPERTY);
                },
                () -> {
                    var value = getFieldSupplierOrDefault(supplier).get();
                    returnVal.put(RESOLUTION_VALUE_KEY, value);
                    returnVal.put(RESOLUTION_METHOD_KEY, isBlank(value) ? ResolvedBy.NONE : ResolvedBy.DEFAULT);
                });

        return Pair.of((String) returnVal.get(RESOLUTION_VALUE_KEY), (ResolvedBy) returnVal.get(RESOLUTION_METHOD_KEY));

    }

    private Supplier<String> getFieldSupplierOrDefault(Supplier<String> supplier) {
        if (isNull(supplier)) {
            return () -> "";
        }

        return supplier;
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
