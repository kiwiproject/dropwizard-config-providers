package org.kiwiproject.config.provider;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Builder;
import lombok.Getter;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.base.KiwiPrimitives;
import org.kiwiproject.config.provider.util.PropertyResolutionSettings;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;
import org.kiwiproject.json.JsonHelper;

import java.util.Map;

/**
 * Config provider that determines the connection information and configuration to send logs to an ELK server.
 * <p>
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.elk.host, kiwi.elk.port, kiwi.elk.customFields</li>
 *     <li>Environment Variable: KIWI_ELK_HOST, KIWI_ELK_PORT, KIWI_ELK_CUSTOM_FIELDS</li>
 *     <li>External Config File: kiwi.elk.host, kiwi.elk.port, kiwi.elk.customFields</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
 */
public class ElkLoggerConfigProvider implements ConfigProvider {

    public static final String DEFAULT_HOST_SYSTEM_PROPERTY = "kiwi.elk.host";

    public static final String DEFAULT_HOST_ENV_VARIABLE = "KIWI_ELK_HOST";

    public static final String DEFAULT_HOST_EXTERNAL_PROPERTY_KEY = "elk.host";

    public static final String DEFAULT_PORT_SYSTEM_PROPERTY = "kiwi.elk.port";

    public static final String DEFAULT_PORT_ENV_VARIABLE = "KIWI_ELK_PORT";

    public static final String DEFAULT_PORT_EXTERNAL_PROPERTY_KEY = "elk.port";

    public static final String DEFAULT_CUSTOM_FIELDS_SYSTEM_PROPERTY = "kiwi.elk.customFields";

    public static final String DEFAULT_CUSTOM_FIELDS_ENV_VARIABLE = "KIWI_ELK_CUSTOM_FIELDS";

    public static final String DEFAULT_CUSTOM_FIELDS_EXTERNAL_PROPERTY_KEY = "elk.customFields";

    @Getter
    private final String host;

    private final ResolvedBy hostResolvedBy;

    @Getter
    private final int port;

    private final ResolvedBy portResolvedBy;

    @Getter
    private final Map<String, String> customFields;

    private final ResolvedBy customFieldsResolvedBy;

    @Builder
    private ElkLoggerConfigProvider(ExternalConfigProvider externalConfigProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    FieldResolverStrategy<String> hostResolverStrategy,
                                    FieldResolverStrategy<Integer> portResolverStrategy,
                                    FieldResolverStrategy<Map<String, String>> customFieldsResolverStrategy) {

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
                .convertFromString(KiwiPrimitives::tryParseIntOrThrow)
                .build());

        var portValue = portResolution.getValue();

        this.port = isNull(portValue) ? 0 : portValue;
        this.portResolvedBy = portResolution.getResolvedBy();

        var json = new JsonHelper();

        var customFieldsResolution = SinglePropertyResolver.resolveProperty(PropertyResolutionSettings.<Map<String, String>>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(customFieldsResolverStrategy)
                .systemProperty(DEFAULT_CUSTOM_FIELDS_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_CUSTOM_FIELDS_ENV_VARIABLE)
                .externalKey(DEFAULT_CUSTOM_FIELDS_EXTERNAL_PROPERTY_KEY)
                .convertFromString(value -> json.toMap(value, new TypeReference<>() {}))
                .build());

        this.customFields = customFieldsResolution.getValue();
        this.customFieldsResolvedBy = customFieldsResolution.getResolvedBy();
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
                "customFields", customFieldsResolvedBy
        );
    }
}
