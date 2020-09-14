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
import java.util.function.Supplier;

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
    private NetworkIdentityProvider(String namedNetwork,
                                    String systemPropertyKey,
                                    String envVariable,
                                    String externalProperty,
                                    ExternalPropertyProvider externalPropertyProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    Supplier<String> networkSupplier) {

        var networkFromSystemProperties = System.getProperty(getSystemPropertyOrDefault(systemPropertyKey));

        var resolvedEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var networkFromEnv = resolvedEnvironment.getenv(getEnvironmentVariableOrDefault(envVariable));

        if (isNotBlank(networkFromSystemProperties)) {
            this.network = networkFromSystemProperties;
            this.networkResolvedBy = ResolvedBy.SYSTEM_PROPERTY;
        } else if (isNotBlank(networkFromEnv)) {
            this.network = networkFromEnv;
            this.networkResolvedBy = ResolvedBy.SYSTEM_ENV;
        } else if (nonNull(namedNetwork)) {
            this.network = namedNetwork;
            this.networkResolvedBy = ResolvedBy.EXPLICIT_VALUE;
        } else {
            var externalPropertyKey = isBlank(externalProperty) ? DEFAULT_EXTERNAL_PROPERTY_KEY : externalProperty;
            getExternalPropertyProviderOrDefault(externalPropertyProvider).usePropertyIfPresent(externalPropertyKey,
                    value -> {
                        this.network = value;
                        this.networkResolvedBy = ResolvedBy.EXTERNAL_PROPERTY;
                    },
                    () -> {
                        this.network = getNamedNetworkSupplierOrDefault(networkSupplier).get();
                        this.networkResolvedBy = isBlank(this.network) ? ResolvedBy.NONE : ResolvedBy.DEFAULT;
                    });
        }
    }

    private String getSystemPropertyOrDefault(String providedPropertyName) {
        return isBlank(providedPropertyName) ? DEFAULT_NETWORK_SYSTEM_PROPERTY : providedPropertyName;
    }

    private String getEnvironmentVariableOrDefault(String providedEnvironmentVariable) {
        return isBlank(providedEnvironmentVariable) ? DEFAULT_NETWORK_ENV_VARIABLE : providedEnvironmentVariable;
    }

    private ExternalPropertyProvider getExternalPropertyProviderOrDefault(ExternalPropertyProvider providedProvider) {
        return nonNull(providedProvider) ? providedProvider : ExternalPropertyProvider.builder().build();
    }

    private Supplier<String> getNamedNetworkSupplierOrDefault(Supplier<String> providedSupplier) {
        if (isNull(providedSupplier)) {
            return () -> "";
        }

        return providedSupplier;
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
