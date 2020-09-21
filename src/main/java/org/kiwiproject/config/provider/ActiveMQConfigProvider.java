package org.kiwiproject.config.provider;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;

import java.util.Map;

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

        var resolution = SinglePropertyResolver.resolveStringProperty(
                externalConfigProvider, kiwiEnvironment, resolverStrategy, DEFAULT_AMQ_SERVERS_SYSTEM_PROPERTY,
                DEFAULT_AMQ_SERVERS_ENV_VARIABLE, DEFAULT_EXTERNAL_PROPERTY_KEY, "");

        this.activeMQServers = resolution.getLeft();
        this.activeMQServersResolvedBy = resolution.getRight();
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
