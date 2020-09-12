package org.kiwiproject.config;

/**
 * Explains how a specific configuration value was resolved.
 */
public enum ResolvedBy {

    /**
     * Resolved by the provider's default mechanism
     */
    DEFAULT,

    /**
     * Resolved from external configuration
     *
     * @see ExternalPropertyProvider
     */
    EXTERNAL_PROPERTY,

    /**
     * Resolved from a system property
     *
     * @see System#getProperty(String)
     */
    SYSTEM_PROPERTY,

    /**
     * Resolved by an explicit value being supplied, e.g. to a constructor or via a (static) factory method.
     */
    EXPLICIT_VALUE,

    /**
     * Resolution did not occur; no value was resolved
     */
    NONE
}
