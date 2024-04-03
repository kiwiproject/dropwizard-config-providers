package org.kiwiproject.config.provider;

import lombok.Value;

/**
 * Contains the result of a property resolution.
 * <p>
 * Was a property resolved? If so, how was it resolved?
 *
 * @see ResolvedBy
 * @see org.kiwiproject.config.provider.util.SinglePropertyResolver
 */
@Value
public class ResolverResult<T> {

    T value;
    ResolvedBy resolvedBy;

    /**
     * Was the property resolved? A property is considered resolved if the {@link ResolvedBy} is
     * any value except {@link ResolvedBy#NONE NONE}.
     *
     * @return true if the property was resolved, otherwise false
     */
    public boolean resolved() {
        return resolvedBy != ResolvedBy.NONE;
    }

    /**
     * Was the property <strong>not</strong> resolved?
     *
     * @return true if the property was <strong>not</strong> resolved, otherwise false
     */
    public boolean notResolved() {
        return !resolved();
    }
}
