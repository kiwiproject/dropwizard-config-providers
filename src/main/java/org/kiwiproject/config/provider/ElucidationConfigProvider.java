package org.kiwiproject.config.provider;

import static java.util.Objects.isNull;
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
 * Config provider that determines the connection information and configuration to send relationship events to Elucidation.
 * <p>
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.elucidation.host, kiwi.elucidation.port, kiwi.elucidation.enabled</li>
 *     <li>Environment Variable: KIWI_ELUCIDATION_HOST, KIWI_ELUCIDATION_PORT, KIWI_ELUCIDATION_ENABLED</li>
 *     <li>External Config File: kiwi.elucidation.host, kiwi.elucidation.port, kiwi.elucidation.enabled</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
 */
@Slf4j
public class ElucidationConfigProvider implements ConfigProvider {

    @VisibleForTesting
    static final String DEFAULT_HOST_SYSTEM_PROPERTY = "kiwi.elucidation.host";

    @VisibleForTesting
    static final String DEFAULT_HOST_ENV_VARIABLE = "KIWI_ELUCIDATION_HOST";

    @VisibleForTesting
    static final String DEFAULT_HOST_EXTERNAL_PROPERTY_KEY = "elucidation.host";

    @VisibleForTesting
    static final String DEFAULT_PORT_SYSTEM_PROPERTY = "kiwi.elucidation.port";

    @VisibleForTesting
    static final String DEFAULT_PORT_ENV_VARIABLE = "KIWI_ELUCIDATION_PORT";

    @VisibleForTesting
    static final String DEFAULT_PORT_EXTERNAL_PROPERTY_KEY = "elucidation.port";

    @VisibleForTesting
    static final String DEFAULT_ENABLED_SYSTEM_PROPERTY = "kiwi.elucidation.enabled";

    @VisibleForTesting
    static final String DEFAULT_ENABLED_ENV_VARIABLE = "KIWI_ELUCIDATION_ENABLED";

    @VisibleForTesting
    static final String DEFAULT_ENABLED_EXTERNAL_PROPERTY_KEY = "elucidation.enabled";

    @Getter
    private final String host;

    private final ResolvedBy hostResolvedBy;

    @Getter
    private final int port;

    private final ResolvedBy portResolvedBy;

    @Getter
    private final boolean enabled;

    private final ResolvedBy enabledResolvedBy;

    @Builder
    private ElucidationConfigProvider(ExternalConfigProvider externalConfigProvider,
                                      KiwiEnvironment kiwiEnvironment,
                                      FieldResolverStrategy<String> hostResolverStrategy,
                                      FieldResolverStrategy<Integer> portResolverStrategy,
                                      FieldResolverStrategy<Boolean> enabledResolverStrategy) {

        var hostResolution = SinglePropertyResolver.resolveStringProperty(PropertyResolutionSettings.<String>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(hostResolverStrategy)
                .systemProperty(DEFAULT_HOST_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_HOST_ENV_VARIABLE)
                .externalKey(DEFAULT_HOST_EXTERNAL_PROPERTY_KEY)
                .build());

        this.host = hostResolution.getValue();
        this.hostResolvedBy = hostResolution.getResolvedBy();

        var portResolution = SinglePropertyResolver.resolveProperty(PropertyResolutionSettings.<Integer>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(portResolverStrategy)
                .systemProperty(DEFAULT_PORT_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_PORT_ENV_VARIABLE)
                .externalKey(DEFAULT_PORT_EXTERNAL_PROPERTY_KEY)
                .convertFromString(Integer::parseInt)
                .build());

        var portValue = portResolution.getValue();

        this.port = isNull(portValue) ? 0 : portValue;
        this.portResolvedBy = portResolution.getResolvedBy();

        var enabledResolution = SinglePropertyResolver.resolveProperty(PropertyResolutionSettings.<Boolean>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(enabledResolverStrategy)
                .systemProperty(DEFAULT_ENABLED_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_ENABLED_ENV_VARIABLE)
                .externalKey(DEFAULT_ENABLED_EXTERNAL_PROPERTY_KEY)
                .convertFromString(Boolean::parseBoolean)
                .build());

        var enabledValue = enabledResolution.getValue();

        this.enabled = !isNull(enabledValue) && enabledValue;
        this.enabledResolvedBy = enabledResolution.getResolvedBy();
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(host) && port > 0;
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of(
                "host", hostResolvedBy,
                "port", portResolvedBy,
                "enabled", enabledResolvedBy
        );
    }
}
