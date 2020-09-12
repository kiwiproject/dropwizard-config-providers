package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

@DisplayName("ExternalPropertyProvider")
class ExternalPropertyProviderTest {

    private ExternalPropertyProvider provider;
    private Path propertyPath;

    @BeforeEach
    void setUp() {
        propertyPath = Path.of(ResourceHelpers.resourceFilePath("ExternalPropertyProvider/config.properties"));
        provider = new ExternalPropertyProvider(propertyPath);
    }

    @Nested
    class Construct {

        @Test
        void shouldCreateDefault_WithoutSystemProperty() {
            var provider = new ExternalPropertyProvider();
            assertThat(provider.canProvide()).isFalse();
            assertThat(provider.getPropertiesPath()).isEqualTo(ExternalPropertyProvider.DEFAULT_CONFIG_PATH);
        }

        @Test
        void shouldCreateDefault_WithSystemProperty() {
            System.setProperty(ExternalPropertyProvider.DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY, "/foo");

            var provider = new ExternalPropertyProvider();
            assertThat(provider.canProvide()).isFalse();
            assertThat(provider.getPropertiesPath()).isEqualTo(Path.of("/foo"));

            System.clearProperty(ExternalPropertyProvider.DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY);
        }

        @Test
        void shouldCreateFromResourcePath() {
            assertThat(provider.canProvide()).isTrue();
            assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
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
