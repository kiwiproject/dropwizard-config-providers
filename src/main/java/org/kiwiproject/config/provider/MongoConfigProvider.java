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
 * Config provider that determines the connection string for Mongo.
 * <p>
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.mongo.connection</li>
 *     <li>Environment Variable: KIWI_MONGO_CONNECTION</li>
 *     <li>External Config File: mongo.connection</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
 */
public class MongoConfigProvider implements ConfigProvider {

    @VisibleForTesting
    static final String DEFAULT_MONGO_SYSTEM_PROPERTY = "kiwi.mongo.connection";

    @VisibleForTesting
    static final String DEFAULT_MONGO_ENV_VARIABLE = "KIWI_MONGO_CONNECTION";

    @VisibleForTesting
    static final String DEFAULT_EXTERNAL_PROPERTY_KEY = "mongo.connection";

    @Getter
    private final String url;

    private final ResolvedBy urlResolvedBy;

    @Builder
    private MongoConfigProvider(ExternalConfigProvider externalConfigProvider,
                                KiwiEnvironment kiwiEnvironment,
                                FieldResolverStrategy<String> resolverStrategy) {

        var resolution = SinglePropertyResolver.resolveStringProperty(PropertyResolutionSettings.<String>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(resolverStrategy)
                .systemProperty(DEFAULT_MONGO_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_MONGO_ENV_VARIABLE)
                .externalKey(DEFAULT_EXTERNAL_PROPERTY_KEY)
                .build());

        this.url = resolution.getValue();
        this.urlResolvedBy = resolution.getResolvedBy();
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(url);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of("url", urlResolvedBy);
    }
}
