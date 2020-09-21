package org.kiwiproject.config.provider;

import java.util.Map;

/**
 * Base definition of a config provider used to assist in providing defaults for a Dropwizard service.
 */
@FunctionalInterface
public interface ConfigProvider {

    /**
     * Determines if a provider has enough environmental awareness to provide data.
     *
     * @return {@code true} if the provider can provide data, otherwise {@code false}
     */
    boolean canProvide();

    /**
     * Returns the opposite value of {@link #canProvide()}.
     *
     * @return opposite of {@link #canProvide()}
     */
    default boolean canNotProvide() {
        return !canProvide();
    }

    /**
     * Returns a mapping of config fields to how the field was resolved.
     *
     * @return a mapping of config fields to how the field was resolved
     * @see ResolvedBy
     */
    default Map<String, ResolvedBy> getResolvedBy() {
        return Map.of();
    }

}
