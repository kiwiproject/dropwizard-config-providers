package org.kiwiproject.config.provider;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;

import java.util.Map;

/**
 * Config provider that determines the identity of the service that is running.  Identity is defined by the service
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
public class ServiceIdentityConfigProvider implements ConfigProvider {

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
    private final String name;

    private final ResolvedBy nameResolvedBy;

    @Getter
    private final String version;

    private final ResolvedBy versionResolvedBy;

    @Getter
    private final String environment;

    private final ResolvedBy environmentResolvedBy;

    @Builder
    private ServiceIdentityConfigProvider(ExternalConfigProvider externalConfigProvider,
                                          KiwiEnvironment kiwiEnvironment,
                                          FieldResolverStrategy<String> nameResolverStrategy,
                                          FieldResolverStrategy<String> versionResolverStrategy,
                                          FieldResolverStrategy<String> environmentResolverStrategy) {

        var nameResolution = SinglePropertyResolver.resolveProperty(
                externalConfigProvider, kiwiEnvironment, nameResolverStrategy, DEFAULT_NAME_SYSTEM_PROPERTY,
                DEFAULT_NAME_ENV_VARIABLE, DEFAULT_NAME_EXTERNAL_PROPERTY_KEY);

        this.name = nameResolution.getLeft();
        this.nameResolvedBy = nameResolution.getRight();

        var versionResolution = SinglePropertyResolver.resolveProperty(
                externalConfigProvider, kiwiEnvironment, versionResolverStrategy, DEFAULT_VERSION_SYSTEM_PROPERTY,
                DEFAULT_VERSION_ENV_VARIABLE, DEFAULT_VERSION_EXTERNAL_PROPERTY_KEY);

        this.version = versionResolution.getLeft();
        this.versionResolvedBy = versionResolution.getRight();

        var environmentResolution = SinglePropertyResolver.resolveProperty(
                externalConfigProvider, kiwiEnvironment, environmentResolverStrategy, DEFAULT_ENVIRONMENT_SYSTEM_PROPERTY,
                DEFAULT_ENVIRONMENT_ENV_VARIABLE, DEFAULT_ENVIRONMENT_EXTERNAL_PROPERTY_KEY);

        this.environment = environmentResolution.getLeft();
        this.environmentResolvedBy = environmentResolution.getRight();
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
