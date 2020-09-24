package org.kiwiproject.config.provider;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.util.PropertyResolutionSettings;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;

import java.util.Map;

/**
 * Config provider that determines the connection string for ActiveMQ.
 * <p>
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.amq.connection</li>
 *     <li>Environment Variable: KIWI_AMQ_CONNECTION</li>
 *     <li>External Config File: amq.connection</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
 */
public class ActiveMQConfigProvider implements ConfigProvider {

    @VisibleForTesting
    static final String DEFAULT_AMQ_SERVERS_SYSTEM_PROPERTY = "kiwi.amq.connection";

    @VisibleForTesting
    static final String DEFAULT_AMQ_SERVERS_ENV_VARIABLE = "KIWI_AMQ_CONNECTION";

    @VisibleForTesting
    static final String DEFAULT_EXTERNAL_PROPERTY_KEY = "amq.connection";

    @Getter
    private final String activeMQServers;

    private final ResolvedBy activeMQServersResolvedBy;

    @Builder
    private ActiveMQConfigProvider(ExternalConfigProvider externalConfigProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    FieldResolverStrategy<String> resolverStrategy) {

        var resolution = SinglePropertyResolver.resolveStringProperty(PropertyResolutionSettings.<String>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(resolverStrategy)
                .systemProperty(DEFAULT_AMQ_SERVERS_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_AMQ_SERVERS_ENV_VARIABLE)
                .externalKey(DEFAULT_EXTERNAL_PROPERTY_KEY)
                .build());

        this.activeMQServers = resolution.getValue();
        this.activeMQServersResolvedBy = resolution.getResolvedBy();
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(activeMQServers);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of("activeMQServers", activeMQServersResolvedBy);
    }
}
