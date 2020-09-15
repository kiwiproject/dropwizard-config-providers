package org.kiwiproject.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.util.Map;

/**
 * Property provider that determines the named network on which the service is running.
 * <p>
 * This is useful when a system of services are deployed in multiple locations like separate AWS VPCs or subnets.
 * <p>
 * The provider will look for the named network in the following order:
 * <ol>
 *     <li>System property with the given system property key</li>
 *     <li>System property with the default system property key (kiwi.network)</li>
 *     <li>Environment variable with the given variable name</li>
 *     <li>Environment variable with the default variable name (KIWI_NETWORK)</li>
 *     <li>The given network</li>
 *     <li>The named network from an external configuration file with the given key</li>
 *     <li>The named network from an external configuration file with the default key (network)</li>
 *     <li>The named network from a given supplier</li>
 * </ol>
 */
@Slf4j
public class NetworkIdentityProvider implements ConfigProvider {

    @VisibleForTesting
    static final String DEFAULT_NETWORK_SYSTEM_PROPERTY = "kiwi.network";

    @VisibleForTesting
    static final String DEFAULT_NETWORK_ENV_VARIABLE = "KIWI_NETWORK";

    @VisibleForTesting
    static final String DEFAULT_EXTERNAL_PROPERTY_KEY = "network";

    @Getter
    private String network;
    private ResolvedBy networkResolvedBy = ResolvedBy.NONE;


    @Builder
    private NetworkIdentityProvider(ExternalPropertyProvider externalPropertyProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    FieldResolverStrategy<String> resolverStrategy) {

        var networkResolver = isNull(resolverStrategy)
                ? FieldResolverStrategy.<String>builder().build() : resolverStrategy;

        var networkFromSystemProperties = System.getProperty(networkResolver.getSystemPropertyKeyOrDefault(DEFAULT_NETWORK_SYSTEM_PROPERTY));

        var resolvedEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var networkFromEnv = resolvedEnvironment.getenv(networkResolver.getEnvVariableOrDefault(DEFAULT_NETWORK_ENV_VARIABLE));

        if (isNotBlank(networkFromSystemProperties)) {
            this.network = networkFromSystemProperties;
            this.networkResolvedBy = ResolvedBy.SYSTEM_PROPERTY;
        } else if (isNotBlank(networkFromEnv)) {
            this.network = networkFromEnv;
            this.networkResolvedBy = ResolvedBy.SYSTEM_ENV;
        } else if (nonNull(networkResolver.getExplicitValue())) {
            this.network = networkResolver.getExplicitValue();
            this.networkResolvedBy = ResolvedBy.EXPLICIT_VALUE;
        } else {
            getExternalPropertyProviderOrDefault(externalPropertyProvider)
                    .usePropertyIfPresent(networkResolver.getExternalPropertyOrDefault(DEFAULT_EXTERNAL_PROPERTY_KEY),
                        value -> {
                            this.network = value;
                            this.networkResolvedBy = ResolvedBy.EXTERNAL_PROPERTY;
                        },
                        () -> {
                            this.network = networkResolver.getValueSupplierOrDefault("").get();
                            this.networkResolvedBy = isBlank(this.network) ? ResolvedBy.NONE : ResolvedBy.DEFAULT;
                        });
        }
    }

    private ExternalPropertyProvider getExternalPropertyProviderOrDefault(ExternalPropertyProvider providedProvider) {
        return nonNull(providedProvider) ? providedProvider : ExternalPropertyProvider.builder().build();
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(network);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of(DEFAULT_EXTERNAL_PROPERTY_KEY, networkResolvedBy);
    }
}
