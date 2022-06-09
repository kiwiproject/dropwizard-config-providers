package org.kiwiproject.config.provider;

import static org.kiwiproject.collect.KiwiMaps.isNotNullOrEmpty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.util.PropertyResolutionSettings;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;
import org.kiwiproject.json.JsonHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Config provider that provides defaults when using Hibernate.
 * <p>
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.hibernate.properties</li>
 *     <li>Environment Variable: KIWI_HIBERNATE_PROPERTIES</li>
 *     <li>External Config File: hibernate.properties</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
 */
public class HibernateConfigProvider implements ConfigProvider {

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

    @VisibleForTesting
    static final String DEFAULT_HIBERNATE_SYSTEM_PROPERTY = "kiwi.hibernate.properties";

    @VisibleForTesting
    static final String DEFAULT_HIBERNATE_ENV_VARIABLE = "KIWI_HIBERNATE_PROPERTIES";

    @VisibleForTesting
    static final String DEFAULT_EXTERNAL_PROPERTY_KEY = "hibernate.properties";

    @VisibleForTesting
    static final Map<String, Object> DEFAULT_HIBERNATE_PROPERTIES = Map.of(
            "hibernate.show_sql", false,
            "hibernate.format_sql", true,
            "hibernate.use_sql_comments", true
    );

    @Getter
    private final Map<String, Object> hibernateProperties;

    private final ResolvedBy hibernatePropertiesResolvedBy;


    @Builder
    private HibernateConfigProvider(ExternalConfigProvider externalConfigProvider,
                                    KiwiEnvironment kiwiEnvironment,
                                    FieldResolverStrategy<Map<String, Object>> resolverStrategy) {

        var resolution = SinglePropertyResolver.resolveProperty(PropertyResolutionSettings.<Map<String, Object>>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(resolverStrategy)
                .systemProperty(DEFAULT_HIBERNATE_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_HIBERNATE_ENV_VARIABLE)
                .externalKey(DEFAULT_EXTERNAL_PROPERTY_KEY)
                .defaultValue(DEFAULT_HIBERNATE_PROPERTIES)
                .convertFromString(value -> JSON_HELPER.toMap(value, new TypeReference<>() {}))
                .build());

        var mergedProperties = new HashMap<>(DEFAULT_HIBERNATE_PROPERTIES);
        mergedProperties.putAll(resolution.getValue());

        this.hibernateProperties = mergedProperties;
        this.hibernatePropertiesResolvedBy = resolution.getResolvedBy();
    }

    @Override
    public boolean canProvide() {
        return isNotNullOrEmpty(hibernateProperties);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of("hibernateProperties", hibernatePropertiesResolvedBy);
    }
}
