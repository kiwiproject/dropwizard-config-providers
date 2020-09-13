package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ConfigProvider")
class ConfigProviderTest {

    @Nested
    class CanProvide {
        @Test
        void shouldReturnTrueWhenCanProvide() {
            ConfigProvider provider = () -> true;
            assertThat(provider.canProvide()).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCanNotProvide() {
            ConfigProvider provider = () -> false;
            assertThat(provider.canProvide()).isFalse();
        }
    }

    @Nested
    class CanNotProvide {
        @Test
        void shouldReturnFalseWhenCanProvide() {
            ConfigProvider provider = () -> true;
            assertThat(provider.canNotProvide()).isFalse();
        }

        @Test
        void shouldReturnTrueWhenCanNotProvide() {
            ConfigProvider provider = () -> false;
            assertThat(provider.canNotProvide()).isTrue();
        }
    }

    @Nested
    class GetResolvedBy {
        @Test
        void shouldReturnAnEmptyMapByDefault() {
            ConfigProvider provider = () -> true;
            assertThat(provider.getResolvedBy()).isEmpty();
        }
    }
}
