package org.kiwiproject.config.provider;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.util.PropertyResolutionSettings;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;

import java.util.Map;

/**
 * Config provider that determines the named network on which the service is running.
 * <p>
 * This is useful when a system of services are deployed in multiple locations like separate AWS VPCs or subnets.
 * <p>
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.network</li>
 *     <li>Environment Variable: KIWI_NETWORK</li>
 *     <li>External Config File: network</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
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

        var resolution = SinglePropertyResolver.resolveStringProperty(PropertyResolutionSettings.<String>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(resolverStrategy)
                .systemProperty(DEFAULT_NETWORK_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_NETWORK_ENV_VARIABLE)
                .externalKey(DEFAULT_EXTERNAL_PROPERTY_KEY)
                .build());

        this.network = resolution.getValue();
        this.networkResolvedBy = resolution.getResolvedBy();
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
