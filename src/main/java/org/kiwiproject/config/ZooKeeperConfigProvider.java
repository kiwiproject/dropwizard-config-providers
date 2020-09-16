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
    private String connectString;

    private ResolvedBy connectStrResolvedBy = ResolvedBy.NONE;


    @Builder
    private ZooKeeperConfigProvider(ExternalConfigProvider externalConfigProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    FieldResolverStrategy<String> resolverStrategy) {

        var connectStrResolver = isNull(resolverStrategy)
                ? FieldResolverStrategy.<String>builder().build() : resolverStrategy;

        var connectStrFromSystemProperties = System.getProperty(
                connectStrResolver.getSystemPropertyKeyOrDefault(DEFAULT_CONNECT_STRING_SYSTEM_PROPERTY));

        var resolvedEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var connectStrFromEnv = resolvedEnvironment.getenv(connectStrResolver.getEnvVariableOrDefault(DEFAULT_CONNECT_STRING_ENV_VARIABLE));

        if (isNotBlank(connectStrFromSystemProperties)) {
            this.connectString = connectStrFromSystemProperties;
            this.connectStrResolvedBy = ResolvedBy.SYSTEM_PROPERTY;
        } else if (isNotBlank(connectStrFromEnv)) {
            this.connectString = connectStrFromEnv;
            this.connectStrResolvedBy = ResolvedBy.SYSTEM_ENV;
        } else if (nonNull(connectStrResolver.getExplicitValue())) {
            this.connectString = connectStrResolver.getExplicitValue();
            this.connectStrResolvedBy = ResolvedBy.EXPLICIT_VALUE;
        } else {
            getExternalPropertyProviderOrDefault(externalConfigProvider)
                    .usePropertyIfPresent(connectStrResolver.getExternalPropertyOrDefault(DEFAULT_EXTERNAL_PROPERTY_KEY),
                        value -> {
                            this.connectString = value;
                            this.connectStrResolvedBy = ResolvedBy.EXTERNAL_PROPERTY;
                        },
                        () -> {
                            this.connectString = connectStrResolver.getValueSupplierOrDefault("").get();
                            this.connectStrResolvedBy = isBlank(this.connectString) ? ResolvedBy.NONE : ResolvedBy.DEFAULT;
                        });
        }
    }

    private ExternalConfigProvider getExternalPropertyProviderOrDefault(ExternalConfigProvider providedProvider) {
        return nonNull(providedProvider) ? providedProvider : ExternalConfigProvider.builder().build();
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
