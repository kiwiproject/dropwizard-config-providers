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
 * Config provider that determines the directory path that will be used for shared storage between services.
 * <p>
 * Default resolution lookup keys are as follows:
 * <ul>
 *     <li>System Property: kiwi.shared.storage.path</li>
 *     <li>Environment Variable: KIWI_SHARED_STORAGE_PATH</li>
 *     <li>External Config File: shared.storage.path</li>
 * </ul>
 * @see SinglePropertyResolver for resolution order
 */
public class SharedStorageConfigProvider implements ConfigProvider {

    @VisibleForTesting
    static final String DEFAULT_SHARED_STORAGE_PATH_SYSTEM_PROPERTY = "kiwi.shared.storage.path";

    @VisibleForTesting
    static final String DEFAULT_SHARED_STORAGE_PATH_ENV_VARIABLE = "KIWI_SHARED_STORAGE_PATH";

    @VisibleForTesting
    static final String DEFAULT_EXTERNAL_PROPERTY_KEY = "shared.storage.path";

    @Getter
    private final String sharedStoragePath;

    private final ResolvedBy sharedStoragePathResolvedBy;

    @Builder
    private SharedStorageConfigProvider(ExternalConfigProvider externalConfigProvider,
                                        KiwiEnvironment kiwiEnvironment,
                                        FieldResolverStrategy<String> resolverStrategy) {

        var resolution = SinglePropertyResolver.resolveStringProperty(PropertyResolutionSettings.<String>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(resolverStrategy)
                .systemProperty(DEFAULT_SHARED_STORAGE_PATH_SYSTEM_PROPERTY)
                .environmentVariable(DEFAULT_SHARED_STORAGE_PATH_ENV_VARIABLE)
                .externalKey(DEFAULT_EXTERNAL_PROPERTY_KEY)
                .build());

        this.sharedStoragePath = resolution.getValue();
        this.sharedStoragePathResolvedBy = resolution.getResolvedBy();
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(sharedStoragePath);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of("sharedStoragePath", sharedStoragePathResolvedBy);
    }
}
