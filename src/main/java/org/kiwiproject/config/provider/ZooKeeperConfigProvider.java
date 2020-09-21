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
 * Config provider that determines the connect string to use for a ZooKeeper connection.
 * <p>
 * This is useful when a system of services are deployed in multiple locations like separate AWS VPCs or subnets.
 * <p>
 * The provider will look for the named network in the following order:
 * <ol>
 *     <li>System property with the given system property key</li>
 *     <li>System property with the default system property key (kiwi.zookeeper.connection)</li>
 *     <li>Environment variable with the given variable name</li>
 *     <li>Environment variable with the default variable name (KIWI_ZOOKEEPER_CONNECTION)</li>
 *     <li>The given ZooKeeper connect string</li>
 *     <li>The ZooKeeper connect string from an external configuration file with the given key</li>
 *     <li>The ZooKeeper connect string from an external configuration file with the default key (zookeeper.connection)</li>
 *     <li>The ZooKeeper connect string from a given supplier</li>
 * </ol>
 */
@Slf4j
public class ZooKeeperConfigProvider implements ConfigProvider {

    @VisibleForTesting
    static final String DEFAULT_CONNECT_STRING_SYSTEM_PROPERTY = "kiwi.zookeeper.connection";

    @VisibleForTesting
    static final String DEFAULT_CONNECT_STRING_ENV_VARIABLE = "KIWI_ZOOKEEPER_CONNECTION";

    @VisibleForTesting
    static final String DEFAULT_EXTERNAL_PROPERTY_KEY = "zookeeper.connection";

    @Getter
    private final String connectString;

    private final ResolvedBy connectStrResolvedBy;


    @Builder
    private ZooKeeperConfigProvider(ExternalConfigProvider externalConfigProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    FieldResolverStrategy<String> resolverStrategy) {

        var resolution = SinglePropertyResolver.resolveStringProperty(
                externalConfigProvider, kiwiEnvironment, resolverStrategy, DEFAULT_CONNECT_STRING_SYSTEM_PROPERTY,
                DEFAULT_CONNECT_STRING_ENV_VARIABLE, DEFAULT_EXTERNAL_PROPERTY_KEY, "");

        this.connectString = resolution.getLeft();
        this.connectStrResolvedBy = resolution.getRight();
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(connectString);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of("connectString", connectStrResolvedBy);
    }
}
