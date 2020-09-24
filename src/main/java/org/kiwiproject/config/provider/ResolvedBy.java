package org.kiwiproject.config.provider;

import org.kiwiproject.base.KiwiEnvironment;

/**
 * Explains how a specific configuration value was resolved.
 */
public enum ResolvedBy {

    /**
     * Resolved by the provider's default mechanism
     */
    PROVIDER_DEFAULT,

    /**
     * Resolved from external configuration
     *
     * @see ExternalConfigProvider
     */
    EXTERNAL_PROPERTY,

    /**
     * Resolved from a system property
     *
     * @see System#getProperty(String)
     */
    SYSTEM_PROPERTY,

    /**
     * Resolved from a system environment variable
     *
     * @see KiwiEnvironment#getenv(String)
     */
    SYSTEM_ENV,

    /**
     * Resolved by an explicit value being supplied, e.g. to a constructor or via a (static) factory method.
     */
    EXPLICIT_VALUE,

    /**
     * Resolved by a provided supplier in the {@link FieldResolverStrategy}
     */
    SUPPLIER,

    /**
     * Resolution did not occur; no value was resolved
     */
    NONE
}
