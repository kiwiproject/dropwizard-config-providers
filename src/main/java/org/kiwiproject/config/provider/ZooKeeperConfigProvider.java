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
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.zookeeper.connection</li>
 *     <li>Environment Variable: KIWI_ZOOKEEPER_CONNECTION</li>
 *     <li>External Config File: zookeeper.connection</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
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

        var resolution = SinglePropertyResolver.resolveProperty(
                externalConfigProvider, kiwiEnvironment, resolverStrategy, DEFAULT_CONNECT_STRING_SYSTEM_PROPERTY,
                DEFAULT_CONNECT_STRING_ENV_VARIABLE, DEFAULT_EXTERNAL_PROPERTY_KEY);

        this.connectString = resolution.getValue();
        this.connectStrResolvedBy = resolution.getResolvedBy();
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
