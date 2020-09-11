package org.kiwiproject.config;

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
}
