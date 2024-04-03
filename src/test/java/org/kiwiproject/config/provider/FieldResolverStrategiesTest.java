package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

@DisplayName("FieldResolverStrategies")
class FieldResolverStrategiesTest {

    @Test
    void shouldCreateEnvVarFieldResolverStrateg() {
        var strategy = FieldResolverStrategies.newEnvVarFieldResolverStrategy("KIWI_USERNAME");

        assertAll(
            () -> assertThat(strategy.getEnvVariableOrDefault(null)).isEqualTo("KIWI_USERNAME"),
            () -> assertThat(strategy.getExplicitValue()).isNull(),
            () -> assertThat(strategy.getExternalPropertyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getSystemPropertyKeyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getValueSupplier()).isNull()
        );
    }

    @Test
    void shouldCreateExplicitValueFieldResolverStrategy() {
        var strategy = FieldResolverStrategies.newExplicitValueFieldResolverStrategy("alice");

        assertAll(
            () -> assertThat(strategy.getEnvVariableOrDefault(null)).isNull(),
            () -> assertThat(strategy.getExplicitValue()).isEqualTo("alice"),
            () -> assertThat(strategy.getExternalPropertyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getSystemPropertyKeyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getValueSupplier()).isNull()
        );
    }

    @Test
    void shouldCreateExternalPropertyFieldResolverStrategy() {
        var strategy = FieldResolverStrategies.newExternalPropertyFieldResolverStrategy("username");

        assertAll(
            () -> assertThat(strategy.getEnvVariableOrDefault(null)).isNull(),
            () -> assertThat(strategy.getExplicitValue()).isNull(),
            () -> assertThat(strategy.getExternalPropertyOrDefault(null)).isEqualTo("username"),
            () -> assertThat(strategy.getSystemPropertyKeyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getValueSupplier()).isNull()
        );
    }

    @Test
    void shouldCreateSystemPropertyFieldResolverStrategy() {
        var strategy = FieldResolverStrategies.newSystemPropertyFieldResolverStrategy("kiwi.username");

        assertAll(
            () -> assertThat(strategy.getEnvVariableOrDefault(null)).isNull(),
            () -> assertThat(strategy.getExplicitValue()).isNull(),
            () -> assertThat(strategy.getExternalPropertyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getSystemPropertyKeyOrDefault(null)).isEqualTo("kiwi.username"),
            () -> assertThat(strategy.getValueSupplier()).isNull()
        );
    }

    @Test
    void shouldCreateSupplierFieldResolverStrategy() {
        var strategy = FieldResolverStrategies.newSupplierFieldResolverStrategy(() -> "diane");

        assertAll(
            () -> assertThat(strategy.getEnvVariableOrDefault(null)).isNull(),
            () -> assertThat(strategy.getExplicitValue()).isNull(),
            () -> assertThat(strategy.getExternalPropertyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getSystemPropertyKeyOrDefault(null)).isNull(),
            () -> assertThat(strategy.getValueSupplier()).extracting(Supplier::get).isEqualTo("diane")
        );
    }
}
