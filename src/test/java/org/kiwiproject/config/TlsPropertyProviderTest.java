package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.config.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.util.SystemPropertyHelper.clearAllSystemProperties;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;

import java.nio.file.Path;
import java.util.List;

@DisplayName("TlsPropertyProvider")
class TlsPropertyProviderTest {

    @Nested
    class Construct {

        @Nested
        class WithSystemProperty {

            @AfterEach
            void tearDown() {
                clearAllSystemProperties();
            }

            @Test
            void shouldBuildUsingDefaultSystemPropertyKey() {
                addSystemProperty(TlsPropertyProvider.DEFAULT_KEYSTORE_PATH_SYSTEM_PROPERTY, "foo-path");
                addSystemProperty(TlsPropertyProvider.DEFAULT_KEYSTORE_PASSWORD_SYSTEM_PROPERTY, "foo-pass");
                addSystemProperty(TlsPropertyProvider.DEFAULT_KEYSTORE_TYPE_SYSTEM_PROPERTY, "foo-type");
                addSystemProperty(TlsPropertyProvider.DEFAULT_TRUSTSTORE_PATH_SYSTEM_PROPERTY, "foo-path");
                addSystemProperty(TlsPropertyProvider.DEFAULT_TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY, "foo-pass");
                addSystemProperty(TlsPropertyProvider.DEFAULT_TRUSTSTORE_TYPE_SYSTEM_PROPERTY, "foo-type");
                addSystemProperty(TlsPropertyProvider.DEFAULT_VERIFY_HOSTNAME_SYSTEM_PROPERTY, "false");
                addSystemProperty(TlsPropertyProvider.DEFAULT_PROTOCOL_SYSTEM_PROPERTY, "foo-protocol");
                addSystemProperty(TlsPropertyProvider.DEFAULT_SUPPORTED_PROTOCOLS_SYSTEM_PROPERTY, "foo,bar,baz");

                var provider = TlsPropertyProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();

                var config = provider.getTlsContextConfiguration();
                assertThat(config.getKeyStorePath()).isEqualTo("foo-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("foo-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("foo-type");
                assertThat(config.getTrustStorePath()).isEqualTo("foo-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("foo-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("foo-type");
                assertThat(config.getProtocol()).isEqualTo("foo-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.SYSTEM_PROPERTY),
                        entry("keyStorePassword", ResolvedBy.SYSTEM_PROPERTY),
                        entry("keyStoreType", ResolvedBy.SYSTEM_PROPERTY),
                        entry("trustStorePath", ResolvedBy.SYSTEM_PROPERTY),
                        entry("trustStorePassword", ResolvedBy.SYSTEM_PROPERTY),
                        entry("trustStoreType", ResolvedBy.SYSTEM_PROPERTY),
                        entry("verifyHostname", ResolvedBy.SYSTEM_PROPERTY),
                        entry("protocol", ResolvedBy.SYSTEM_PROPERTY),
                        entry("supportedProtocols", ResolvedBy.SYSTEM_PROPERTY)
                );
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("a", "foo-path");
                addSystemProperty("b", "foo-pass");
                addSystemProperty("c", "foo-type");
                addSystemProperty("d", "foo-path");
                addSystemProperty("e", "foo-pass");
                addSystemProperty("f", "foo-type");
                addSystemProperty("g", "false");
                addSystemProperty("h", "foo-protocol");
                addSystemProperty("i", "foo,bar,baz");

                var provider = TlsPropertyProvider.builder()
                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("a").build())
                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("b").build())
                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("c").build())
                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("d").build())
                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("e").build())
                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("f").build())
                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().systemPropertyKey("g").build())
                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("h").build())
                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().systemPropertyKey("i").build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("foo-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("foo-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("foo-type");
                assertThat(config.getTrustStorePath()).isEqualTo("foo-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("foo-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("foo-type");
                assertThat(config.getProtocol()).isEqualTo("foo-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.SYSTEM_PROPERTY),
                        entry("keyStorePassword", ResolvedBy.SYSTEM_PROPERTY),
                        entry("keyStoreType", ResolvedBy.SYSTEM_PROPERTY),
                        entry("trustStorePath", ResolvedBy.SYSTEM_PROPERTY),
                        entry("trustStorePassword", ResolvedBy.SYSTEM_PROPERTY),
                        entry("trustStoreType", ResolvedBy.SYSTEM_PROPERTY),
                        entry("verifyHostname", ResolvedBy.SYSTEM_PROPERTY),
                        entry("protocol", ResolvedBy.SYSTEM_PROPERTY),
                        entry("supportedProtocols", ResolvedBy.SYSTEM_PROPERTY)
                );
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(TlsPropertyProvider.DEFAULT_KEYSTORE_PATH_ENV_VARIABLE)).thenReturn("bar-path");
                when(env.getenv(TlsPropertyProvider.DEFAULT_KEYSTORE_PASSWORD_ENV_VARIABLE)).thenReturn("bar-pass");
                when(env.getenv(TlsPropertyProvider.DEFAULT_KEYSTORE_TYPE_ENV_VARIABLE)).thenReturn("bar-type");
                when(env.getenv(TlsPropertyProvider.DEFAULT_TRUSTSTORE_PATH_ENV_VARIABLE)).thenReturn("bar-path");
                when(env.getenv(TlsPropertyProvider.DEFAULT_TRUSTSTORE_PASSWORD_ENV_VARIABLE)).thenReturn("bar-pass");
                when(env.getenv(TlsPropertyProvider.DEFAULT_TRUSTSTORE_TYPE_ENV_VARIABLE)).thenReturn("bar-type");
                when(env.getenv(TlsPropertyProvider.DEFAULT_VERIFY_HOSTNAME_ENV_VARIABLE)).thenReturn("bar-host");
                when(env.getenv(TlsPropertyProvider.DEFAULT_PROTOCOL_ENV_VARIABLE)).thenReturn("bar-protocol");
                when(env.getenv(TlsPropertyProvider.DEFAULT_SUPPORTED_PROTOCOLS_ENV_VARIABLE)).thenReturn("bar,foo,baz");

                var provider = TlsPropertyProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("bar-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("bar-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("bar-type");
                assertThat(config.getTrustStorePath()).isEqualTo("bar-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("bar-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("bar-type");
                assertThat(config.getProtocol()).isEqualTo("bar-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.SYSTEM_ENV),
                        entry("keyStorePassword", ResolvedBy.SYSTEM_ENV),
                        entry("keyStoreType", ResolvedBy.SYSTEM_ENV),
                        entry("trustStorePath", ResolvedBy.SYSTEM_ENV),
                        entry("trustStorePassword", ResolvedBy.SYSTEM_ENV),
                        entry("trustStoreType", ResolvedBy.SYSTEM_ENV),
                        entry("verifyHostname", ResolvedBy.SYSTEM_ENV),
                        entry("protocol", ResolvedBy.SYSTEM_ENV),
                        entry("supportedProtocols", ResolvedBy.SYSTEM_ENV)
                );
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("a")).thenReturn("bar-path");
                when(env.getenv("b")).thenReturn("bar-pass");
                when(env.getenv("c")).thenReturn("bar-type");
                when(env.getenv("d")).thenReturn("bar-path");
                when(env.getenv("e")).thenReturn("bar-pass");
                when(env.getenv("f")).thenReturn("bar-type");
                when(env.getenv("g")).thenReturn("bar-host");
                when(env.getenv("h")).thenReturn("bar-protocol");
                when(env.getenv("i")).thenReturn("bar,foo,baz");

                var provider = TlsPropertyProvider.builder()
                        .kiwiEnvironment(env)
                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("a").build())
                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("b").build())
                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("c").build())
                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("d").build())
                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("e").build())
                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("f").build())
                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().envVariable("g").build())
                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("h").build())
                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().envVariable("i").build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("bar-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("bar-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("bar-type");
                assertThat(config.getTrustStorePath()).isEqualTo("bar-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("bar-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("bar-type");
                assertThat(config.getProtocol()).isEqualTo("bar-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.SYSTEM_ENV),
                        entry("keyStorePassword", ResolvedBy.SYSTEM_ENV),
                        entry("keyStoreType", ResolvedBy.SYSTEM_ENV),
                        entry("trustStorePath", ResolvedBy.SYSTEM_ENV),
                        entry("trustStorePassword", ResolvedBy.SYSTEM_ENV),
                        entry("trustStoreType", ResolvedBy.SYSTEM_ENV),
                        entry("verifyHostname", ResolvedBy.SYSTEM_ENV),
                        entry("protocol", ResolvedBy.SYSTEM_ENV),
                        entry("supportedProtocols", ResolvedBy.SYSTEM_ENV)
                );
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalPropertyProvider externalPropertyProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("TlsPropertyProvider/config.properties"));
                externalPropertyProvider = ExternalPropertyProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = TlsPropertyProvider.builder().externalPropertyProvider(externalPropertyProvider).build();
                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("baz-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("baz-type");
                assertThat(config.getTrustStorePath()).isEqualTo("baz-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("baz-type");
                assertThat(config.getProtocol()).isEqualTo("baz-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("keyStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("keyStoreType", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("trustStorePath", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("trustStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("trustStoreType", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("verifyHostname", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("protocol", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("supportedProtocols", ResolvedBy.EXTERNAL_PROPERTY)
                );
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = TlsPropertyProvider.builder()
                        .externalPropertyProvider(externalPropertyProvider)
                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("a").build())
                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("b").build())
                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("c").build())
                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("d").build())
                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("e").build())
                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("f").build())
                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().externalProperty("g").build())
                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("h").build())
                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().externalProperty("i").build())
                        .build();
                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("baz-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("baz-type");
                assertThat(config.getTrustStorePath()).isEqualTo("baz-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("baz-type");
                assertThat(config.getProtocol()).isEqualTo("baz-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("keyStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("keyStoreType", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("trustStorePath", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("trustStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("trustStoreType", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("verifyHostname", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("protocol", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("supportedProtocols", ResolvedBy.EXTERNAL_PROPERTY)
                );
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
                var provider = TlsPropertyProvider.builder()
                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue("baz-path").build())
                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue("baz-pass").build())
                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue("baz-type").build())
                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue("baz-path").build())
                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue("baz-pass").build())
                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue("baz-type").build())
                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().explicitValue(false).build())
                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue("baz-protocol").build())
                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().explicitValue(List.of("foo", "bar", "baz")).build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("baz-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("baz-type");
                assertThat(config.getTrustStorePath()).isEqualTo("baz-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("baz-type");
                assertThat(config.getProtocol()).isEqualTo("baz-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.EXPLICIT_VALUE),
                        entry("keyStorePassword", ResolvedBy.EXPLICIT_VALUE),
                        entry("keyStoreType", ResolvedBy.EXPLICIT_VALUE),
                        entry("trustStorePath", ResolvedBy.EXPLICIT_VALUE),
                        entry("trustStorePassword", ResolvedBy.EXPLICIT_VALUE),
                        entry("trustStoreType", ResolvedBy.EXPLICIT_VALUE),
                        entry("verifyHostname", ResolvedBy.EXPLICIT_VALUE),
                        entry("protocol", ResolvedBy.EXPLICIT_VALUE),
                        entry("supportedProtocols", ResolvedBy.EXPLICIT_VALUE)
                );
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = TlsPropertyProvider.builder()
                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> "baz-path").build())
                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> "baz-pass").build())
                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> "baz-type").build())
                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> "baz-path").build())
                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> "baz-pass").build())
                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> "baz-type").build())
                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().valueSupplier(() -> false).build())
                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> "baz-protocol").build())
                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder()
                                .valueSupplier(() -> List.of("foo", "bar", "baz")).build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("baz-path");
                assertThat(config.getKeyStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getKeyStoreType()).isEqualTo("baz-type");
                assertThat(config.getTrustStorePath()).isEqualTo("baz-path");
                assertThat(config.getTrustStorePassword()).isEqualTo("baz-pass");
                assertThat(config.getTrustStoreType()).isEqualTo("baz-type");
                assertThat(config.getProtocol()).isEqualTo("baz-protocol");
                assertThat(config.getSupportedProtocols()).contains("foo","bar","baz");
                assertThat(config.isVerifyHostname()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.DEFAULT),
                        entry("keyStorePassword", ResolvedBy.DEFAULT),
                        entry("keyStoreType", ResolvedBy.DEFAULT),
                        entry("trustStorePath", ResolvedBy.DEFAULT),
                        entry("trustStorePassword", ResolvedBy.DEFAULT),
                        entry("trustStoreType", ResolvedBy.DEFAULT),
                        entry("verifyHostname", ResolvedBy.DEFAULT),
                        entry("protocol", ResolvedBy.DEFAULT),
                        entry("supportedProtocols", ResolvedBy.DEFAULT)
                );
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCanProvide() {
                var provider = TlsPropertyProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isBlank();
                assertThat(config.getKeyStorePassword()).isBlank();
                assertThat(config.getKeyStoreType()).isEqualTo("JKS");
                assertThat(config.getTrustStorePath()).isBlank();
                assertThat(config.getTrustStorePassword()).isBlank();
                assertThat(config.getTrustStoreType()).isEqualTo("JKS");
                assertThat(config.getProtocol()).isEqualTo("TLSv1.2");
                assertThat(config.getSupportedProtocols()).isNull();
                assertThat(config.isVerifyHostname()).isTrue();
                assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", ResolvedBy.NONE),
                        entry("keyStorePassword", ResolvedBy.NONE),
                        entry("keyStoreType", ResolvedBy.DEFAULT),
                        entry("trustStorePath", ResolvedBy.NONE),
                        entry("trustStorePassword", ResolvedBy.NONE),
                        entry("trustStoreType", ResolvedBy.DEFAULT),
                        entry("verifyHostname", ResolvedBy.DEFAULT),
                        entry("protocol", ResolvedBy.DEFAULT),
                        entry("supportedProtocols", ResolvedBy.NONE)
                );
            }

        }

        @Nested
        class WithProvidedTlsContext {

            @Test
            void shouldBuildUsingTheProvidedContextAsDefault() {
                var context = TlsContextConfiguration.builder().keyStorePath("my-secret-key").build();

                var provider = TlsPropertyProvider.builder().tlsContextConfigurationSupplier(() -> context).build();

                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertThat(config.getKeyStorePath()).isEqualTo("my-secret-key");
                assertThat(config.getKeyStorePassword()).isBlank();
                assertThat(config.getKeyStoreType()).isEqualTo("JKS");
                assertThat(config.getTrustStorePath()).isBlank();
                assertThat(config.getTrustStorePassword()).isBlank();
                assertThat(config.getTrustStoreType()).isEqualTo("JKS");
                assertThat(config.getProtocol()).isEqualTo("TLSv1.2");
                assertThat(config.getSupportedProtocols()).isNull();
                assertThat(config.isVerifyHostname()).isTrue();
                assertThat(provider.getResolvedBy()).contains(
                        entry("tlsContextConfiguration", ResolvedBy.EXPLICIT_VALUE),
                        entry("keyStorePath", ResolvedBy.DEFAULT),
                        entry("keyStorePassword", ResolvedBy.NONE),
                        entry("keyStoreType", ResolvedBy.DEFAULT),
                        entry("trustStorePath", ResolvedBy.NONE),
                        entry("trustStorePassword", ResolvedBy.NONE),
                        entry("trustStoreType", ResolvedBy.DEFAULT),
                        entry("verifyHostname", ResolvedBy.DEFAULT),
                        entry("protocol", ResolvedBy.DEFAULT),
                        entry("supportedProtocols", ResolvedBy.NONE)
                );
            }
        }

    }
}
