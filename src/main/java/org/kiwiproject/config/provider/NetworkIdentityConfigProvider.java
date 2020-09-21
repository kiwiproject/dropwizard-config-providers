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
 * Config provider that determines the named network on which the service is running.
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
public class NetworkIdentityConfigProvider implements ConfigProvider {

    @VisibleForTesting
    static final String DEFAULT_NETWORK_SYSTEM_PROPERTY = "kiwi.network";

    @VisibleForTesting
    static final String DEFAULT_NETWORK_ENV_VARIABLE = "KIWI_NETWORK";

    @VisibleForTesting
    static final String DEFAULT_EXTERNAL_PROPERTY_KEY = "network";

    @Getter
    private final String network;
    private final ResolvedBy networkResolvedBy;


    @Builder
    private NetworkIdentityConfigProvider(ExternalConfigProvider externalConfigProvider,
                                          KiwiEnvironment kiwiEnvironment,
                                          FieldResolverStrategy<String> resolverStrategy) {

        var resolution = SinglePropertyResolver.resolveStringProperty(
                externalConfigProvider, kiwiEnvironment, resolverStrategy, DEFAULT_NETWORK_SYSTEM_PROPERTY,
                DEFAULT_NETWORK_ENV_VARIABLE, DEFAULT_EXTERNAL_PROPERTY_KEY, "");

        this.network = resolution.getLeft();
        this.networkResolvedBy = resolution.getRight();
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
