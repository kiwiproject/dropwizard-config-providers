package org.kiwiproject.config;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

/**
 * Property provider that determines the named network on which the service is running.
 * <p>
 * This is useful when a system of services are deployed in multiple locations like separate AWS VPCs or subnets.
 */
@Slf4j
public class NetworkIdentityProvider implements ConfigProvider {

    @VisibleForTesting
    static final String PROPERTY_KEY = "network";

    protected KiwiEnvironment environment;
    protected String network;
    protected ResolvedBy networkResolvedBy = ResolvedBy.NONE;

    /**
     * Creates the provider with the default {@link ExternalPropertyProvider} and {@link DefaultEnvironment}
     */
    public NetworkIdentityProvider() {
        this(new ExternalPropertyProvider(), new DefaultEnvironment());
    }

    /**
     * Creates the provider with the provided explicit network
     *
     * @param network The explicit network to set
     */
    public NetworkIdentityProvider(String network) {
        this.network = network;
        this.networkResolvedBy = ResolvedBy.EXPLICIT_VALUE;
    }

    /**
     * Creates the provider with the provided {@link ExternalPropertyProvider} and provided {@link KiwiEnvironment}
     *
     * @param propertyProvider  {@link ExternalPropertyProvider} that can resolve the network name from a properties file
     * @param environment       {@link KiwiEnvironment} used to find environment variables on the system
     */
    public NetworkIdentityProvider(ExternalPropertyProvider propertyProvider, KiwiEnvironment environment) {
        this.environment = environment;
        propertyProvider.usePropertyIfPresent(PROPERTY_KEY,
                this::setNetworkFromExternal,
                this::setNetworkFromEnv);
    }

    private void setNetworkFromExternal(String network) {
        this.network = network;
        this.networkResolvedBy = ResolvedBy.EXTERNAL_PROPERTY;
    }

    /**
     * Will look in the system environment for {@code KIWI_ENV_NETWORK}. Subclass this provider and override this method
     * if you want to change the env variable name or set a default value if not found.
     */
    protected void setNetworkFromEnv() {
        var networkEnv = environment.getenv("KIWI_ENV_NETWORK");
        if (isBlank(networkEnv)) {
            LOG.warn("No KIWI_ENV_NETWORK environment variable is present.  Unable to default.");
        } else {
            network = networkEnv;
            networkResolvedBy = ResolvedBy.SYSTEM_ENV;
        }
    }

    /**
     * Identifies whether this instance can provide the network location.
     *
     * @return {@code true} if network resolved, otherwise {@code false}
     */
    @Override
    public boolean canProvide() {
        return isNotBlank(network);
    }
}
