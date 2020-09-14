package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;

import java.nio.file.Path;

@DisplayName("ExternalPropertyProvider")
class ExternalPropertyProviderTest {

    private ExternalPropertyProvider provider;
    private Path propertyPath;

    @BeforeEach
    void setUp() {
        propertyPath = Path.of(ResourceHelpers.resourceFilePath("ExternalPropertyProvider/config.properties"));
        provider = ExternalPropertyProvider.builder().explicitPath(propertyPath).build();
    }

    @Nested
    class Construct {

        @Nested
        class WithSystemProperty {

            @Test
            void shouldBuildUsingDefaultSystemPropertyKey() {
                System.setProperty(ExternalPropertyProvider.DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY, propertyPath.toString());

                var provider = ExternalPropertyProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);

                System.clearProperty(ExternalPropertyProvider.DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                System.setProperty("bar", propertyPath.toString());

                var provider = ExternalPropertyProvider.builder().systemPropertyKey("bar").build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);

                System.clearProperty("bar");
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(ExternalPropertyProvider.DEFAULT_CONFIG_PATH_ENV_VARIABLE)).thenReturn(propertyPath.toString());

                var provider = ExternalPropertyProvider.builder().environment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("baz")).thenReturn(propertyPath.toString());

                var provider = ExternalPropertyProvider.builder()
                        .environment(env)
                        .envVariable("baz")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

        }

        @Nested
        class WithExplicitPath {

            @Test
            void shouldBuildUsingProvidedPath() {
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

        }

        @Nested
        class WithDefaultPath {

            @Test
            void shouldBuildUsingDefaultPath() {
                var provider = ExternalPropertyProvider.builder().build();

                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getPropertiesPath()).isEqualTo(ExternalPropertyProvider.DEFAULT_CONFIG_PATH);
            }

        }

    }

    @Nested
    class GetProperty {

        @Test
        void shouldReturnPropertyValue_WhenFound() {
            var propertyValue = provider.getProperty("unit.test.foo");
            assertThat(propertyValue).hasValue("bar");
        }

        @Test
        void shouldReturnOptionalEmpty_WhenNotFound() {
            var propertyValue = provider.getProperty("unit.test.baz");
            assertThat(propertyValue).isNotPresent();
        }
    }

    @Nested
    class UsePropertyIfPresent {

        @Test
        void shouldCallConsumer_WhenFound() {
            provider.usePropertyIfPresent("unit.test.foo",
                    value -> assertThat(value).isEqualTo("bar"),
                    () -> fail("Test should have called the consumer not the else"));
        }

        @Test
        @SuppressWarnings("java:S3415")
        void shouldCallOrElse_WhenNotFound() {
            provider.usePropertyIfPresent("unit.test.baz",
                    value -> fail("Test should have called the else not the consumer"),
                    () -> assertThat(true).isTrue());
        }
    }
}
