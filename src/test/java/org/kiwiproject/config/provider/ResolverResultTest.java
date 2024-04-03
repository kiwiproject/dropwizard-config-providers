package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

@DisplayName("ResolverResult")
class ResolverResultTest {

    @ParameterizedTest
    @EnumSource(
        value = ResolvedBy.class,
        names = { "NONE" },
        mode = Mode.EXCLUDE)
    void shouldBeResolved_ForAllValuesOtherThanNone(ResolvedBy resolvedBy) {
        var result = new ResolverResult<>("some value", resolvedBy);

        assertAll(
            () -> assertThat(result.resolved()).isTrue(),
            () -> assertThat(result.notResolved()).isFalse()
        );
    }

    @Test
    void shouldNotBeResolved_WhenResolvedByIsNone() {
        var result = new ResolverResult<>(null, ResolvedBy.NONE);

        assertAll(
            () -> assertThat(result.resolved()).isFalse(),
            () -> assertThat(result.notResolved()).isTrue()
        );
    }
}
