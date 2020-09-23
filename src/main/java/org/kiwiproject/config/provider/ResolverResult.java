package org.kiwiproject.config.provider;

import lombok.Value;

@Value
public class ResolverResult<T> {

    T value;
    ResolvedBy resolvedBy;

}
