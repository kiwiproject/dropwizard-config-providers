package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.clearAllSystemProperties;
import static org.kiwiproject.config.provider.util.TestHelpers.mockEnvToReturn;
import static org.kiwiproject.config.provider.util.TestHelpers.newEnvVarFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newExplicitValueFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newExternalPropertyFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newSupplierFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newSystemPropertyFieldResolverStrategy;
import static org.mockito.Mockito.mock;

import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.TlsContextConfiguration;

import java.nio.file.Path;
import java.util.List;

@DisplayName("TlsConfigProvider")
class TlsConfigProviderTest {

    private static final String STORE_PATH = "/keystore/path.jks";
    private static final String STORE_PASSWORD = "keystore-pass";
    private static final String STORE_TYPE = "JKS";
    private static final String PROTOCOL = "TLSv1.2";
    private static final String SUPPORTED_PROTOCOLS = "TLSv1.2,TLSv1.3";
    private static final String[] SUPPORTED_PROTOCOLS_ARRAY = new String[] { "TLSv1.2", "TLSv1.3" };

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
                addSystemProperty(TlsConfigProvider.DEFAULT_KEYSTORE_PATH_SYSTEM_PROPERTY, STORE_PATH);
                addSystemProperty(TlsConfigProvider.DEFAULT_KEYSTORE_PASSWORD_SYSTEM_PROPERTY, STORE_PASSWORD);
                addSystemProperty(TlsConfigProvider.DEFAULT_KEYSTORE_TYPE_SYSTEM_PROPERTY, STORE_TYPE);
                addSystemProperty(TlsConfigProvider.DEFAULT_TRUSTSTORE_PATH_SYSTEM_PROPERTY, STORE_PATH);
                addSystemProperty(TlsConfigProvider.DEFAULT_TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY, STORE_PASSWORD);
                addSystemProperty(TlsConfigProvider.DEFAULT_TRUSTSTORE_TYPE_SYSTEM_PROPERTY, STORE_TYPE);
                addSystemProperty(TlsConfigProvider.DEFAULT_VERIFY_HOSTNAME_SYSTEM_PROPERTY, "false");
                addSystemProperty(TlsConfigProvider.DEFAULT_DISABLE_SNI_HOST_CHECK_SYSTEM_PROPERTY, "true");
                addSystemProperty(TlsConfigProvider.DEFAULT_PROTOCOL_SYSTEM_PROPERTY, PROTOCOL);
                addSystemProperty(TlsConfigProvider.DEFAULT_SUPPORTED_PROTOCOLS_SYSTEM_PROPERTY, SUPPORTED_PROTOCOLS);

                var provider = TlsConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.SYSTEM_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("a", STORE_PATH);
                addSystemProperty("b", STORE_PASSWORD);
                addSystemProperty("c", STORE_TYPE);
                addSystemProperty("d", STORE_PATH);
                addSystemProperty("e", STORE_PASSWORD);
                addSystemProperty("f", STORE_TYPE);
                addSystemProperty("g", "false");
                addSystemProperty("h", "true");
                addSystemProperty("i", PROTOCOL);
                addSystemProperty("j", SUPPORTED_PROTOCOLS);

                var provider = TlsConfigProvider.builder()
                        .keyStorePathResolverStrategy(newSystemPropertyFieldResolverStrategy("a"))
                        .keyStorePasswordResolverStrategy(newSystemPropertyFieldResolverStrategy("b"))
                        .keyStoreTypeResolverStrategy(newSystemPropertyFieldResolverStrategy("c"))
                        .trustStorePathResolverStrategy(newSystemPropertyFieldResolverStrategy("d"))
                        .trustStorePasswordResolverStrategy(newSystemPropertyFieldResolverStrategy("e"))
                        .trustStoreTypeResolverStrategy(newSystemPropertyFieldResolverStrategy("f"))
                        .verifyHostnameResolverStrategy(newSystemPropertyFieldResolverStrategy("g"))
                        .disableSniHostCheckResolverStrategy(newSystemPropertyFieldResolverStrategy("h"))
                        .protocolResolverStrategy(newSystemPropertyFieldResolverStrategy("i"))
                        .supportedProtocolsResolverStrategy(newSystemPropertyFieldResolverStrategy("j"))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.SYSTEM_PROPERTY);
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_KEYSTORE_PATH_ENV_VARIABLE, STORE_PATH);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_KEYSTORE_PASSWORD_ENV_VARIABLE, STORE_PASSWORD);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_KEYSTORE_TYPE_ENV_VARIABLE, STORE_TYPE);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_TRUSTSTORE_PATH_ENV_VARIABLE, STORE_PATH);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_TRUSTSTORE_PASSWORD_ENV_VARIABLE, STORE_PASSWORD);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_TRUSTSTORE_TYPE_ENV_VARIABLE, STORE_TYPE);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_VERIFY_HOSTNAME_ENV_VARIABLE, "false");
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_DISABLE_SNI_HOST_CHECK_ENV_VARIABLE, "true");
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_PROTOCOL_ENV_VARIABLE, PROTOCOL);
                mockEnvToReturn(env, TlsConfigProvider.DEFAULT_SUPPORTED_PROTOCOLS_ENV_VARIABLE, SUPPORTED_PROTOCOLS);

                var provider = TlsConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.SYSTEM_ENV);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                mockEnvToReturn(env, "a", STORE_PATH);
                mockEnvToReturn(env, "b", STORE_PASSWORD);
                mockEnvToReturn(env, "c", STORE_TYPE);
                mockEnvToReturn(env, "d", STORE_PATH);
                mockEnvToReturn(env, "e", STORE_PASSWORD);
                mockEnvToReturn(env, "f", STORE_TYPE);
                mockEnvToReturn(env, "g", "false");
                mockEnvToReturn(env, "h", "true");
                mockEnvToReturn(env, "i", PROTOCOL);
                mockEnvToReturn(env, "j", SUPPORTED_PROTOCOLS);

                var provider = TlsConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .keyStorePathResolverStrategy(newEnvVarFieldResolverStrategy("a"))
                        .keyStorePasswordResolverStrategy(newEnvVarFieldResolverStrategy("b"))
                        .keyStoreTypeResolverStrategy(newEnvVarFieldResolverStrategy("c"))
                        .trustStorePathResolverStrategy(newEnvVarFieldResolverStrategy("d"))
                        .trustStorePasswordResolverStrategy(newEnvVarFieldResolverStrategy("e"))
                        .trustStoreTypeResolverStrategy(newEnvVarFieldResolverStrategy("f"))
                        .verifyHostnameResolverStrategy(newEnvVarFieldResolverStrategy("g"))
                        .disableSniHostCheckResolverStrategy(newEnvVarFieldResolverStrategy("h"))
                        .protocolResolverStrategy(newEnvVarFieldResolverStrategy("i"))
                        .supportedProtocolsResolverStrategy(newEnvVarFieldResolverStrategy("j"))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.SYSTEM_ENV);
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("TlsConfigProvider/config.properties"));
                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = TlsConfigProvider.builder().externalConfigProvider(externalConfigProvider).build();
                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.EXTERNAL_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = TlsConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .keyStorePathResolverStrategy(newExternalPropertyFieldResolverStrategy("a"))
                        .keyStorePasswordResolverStrategy(newExternalPropertyFieldResolverStrategy("b"))
                        .keyStoreTypeResolverStrategy(newExternalPropertyFieldResolverStrategy("c"))
                        .trustStorePathResolverStrategy(newExternalPropertyFieldResolverStrategy("d"))
                        .trustStorePasswordResolverStrategy(newExternalPropertyFieldResolverStrategy("e"))
                        .trustStoreTypeResolverStrategy(newExternalPropertyFieldResolverStrategy("f"))
                        .verifyHostnameResolverStrategy(newExternalPropertyFieldResolverStrategy("g"))
                        .disableSniHostCheckResolverStrategy(newExternalPropertyFieldResolverStrategy("h"))
                        .protocolResolverStrategy(newExternalPropertyFieldResolverStrategy("i"))
                        .supportedProtocolsResolverStrategy(newExternalPropertyFieldResolverStrategy("j"))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.EXTERNAL_PROPERTY);
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
                var provider = TlsConfigProvider.builder()
                        .keyStorePathResolverStrategy(newExplicitValueFieldResolverStrategy(STORE_PATH))
                        .keyStorePasswordResolverStrategy(newExplicitValueFieldResolverStrategy(STORE_PASSWORD))
                        .keyStoreTypeResolverStrategy(newExplicitValueFieldResolverStrategy(STORE_TYPE))
                        .trustStorePathResolverStrategy(newExplicitValueFieldResolverStrategy(STORE_PATH))
                        .trustStorePasswordResolverStrategy(newExplicitValueFieldResolverStrategy(STORE_PASSWORD))
                        .trustStoreTypeResolverStrategy(newExplicitValueFieldResolverStrategy(STORE_TYPE))
                        .verifyHostnameResolverStrategy(newExplicitValueFieldResolverStrategy(false))
                        .disableSniHostCheckResolverStrategy(newExplicitValueFieldResolverStrategy(true))
                        .protocolResolverStrategy(newExplicitValueFieldResolverStrategy(PROTOCOL))
                        .supportedProtocolsResolverStrategy(newExplicitValueFieldResolverStrategy(List.of(SUPPORTED_PROTOCOLS_ARRAY)))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.EXPLICIT_VALUE);
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = TlsConfigProvider.builder()
                        .keyStorePathResolverStrategy(newSupplierFieldResolverStrategy(() -> STORE_PATH))
                        .keyStorePasswordResolverStrategy(newSupplierFieldResolverStrategy(() -> STORE_PASSWORD))
                        .keyStoreTypeResolverStrategy(newSupplierFieldResolverStrategy(() -> STORE_TYPE))
                        .trustStorePathResolverStrategy(newSupplierFieldResolverStrategy(() -> STORE_PATH))
                        .trustStorePasswordResolverStrategy(newSupplierFieldResolverStrategy(() -> STORE_PASSWORD))
                        .trustStoreTypeResolverStrategy(newSupplierFieldResolverStrategy(() -> STORE_TYPE))
                        .verifyHostnameResolverStrategy(newSupplierFieldResolverStrategy(() -> false))
                        .disableSniHostCheckResolverStrategy(newSupplierFieldResolverStrategy(() -> true))
                        .protocolResolverStrategy(newSupplierFieldResolverStrategy(() -> PROTOCOL))
                        .supportedProtocolsResolverStrategy(newSupplierFieldResolverStrategy(() -> List.of(SUPPORTED_PROTOCOLS_ARRAY)))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertContextIsCorrect(provider.getTlsContextConfiguration(), provider, ResolvedBy.SUPPLIER);
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                assertThat(TlsConfigProvider.builder().build().canProvide()).isFalse();

                assertThat(TlsConfigProvider.builder()
                        .trustStorePathResolverStrategy(newExplicitValueFieldResolverStrategy("my-path"))
                        .build()
                        .canProvide()).isFalse();

                assertThat(TlsConfigProvider.builder()
                        .trustStorePasswordResolverStrategy(newExplicitValueFieldResolverStrategy("pass"))
                        .build()
                        .canProvide()).isFalse();
            }

        }

        @Nested
        class WithProvidedTlsContext {

            @Test
            void shouldBuildUsingTheProvidedContextAsDefault() {
                var context = TlsContextConfiguration.builder()
                        .keyStorePath("my-secret-key")
                        .trustStorePath("my-secret-trust")
                        .trustStorePassword("pass")
                        .build();

                var provider = TlsConfigProvider.builder().tlsContextConfigurationSupplier(() -> context).build();

                assertThat(provider.canProvide()).isTrue();
                var config = provider.getTlsContextConfiguration();

                assertAll(
                        () -> assertThat(config.getKeyStorePath()).isEqualTo("my-secret-key"),
                        () -> assertThat(config.getKeyStorePassword()).isBlank(),
                        () -> assertThat(config.getKeyStoreType()).isEqualTo("JKS"),
                        () -> assertThat(config.getTrustStorePath()).isEqualTo("my-secret-trust"),
                        () -> assertThat(config.getTrustStorePassword()).isEqualTo("pass"),
                        () -> assertThat(config.getTrustStoreType()).isEqualTo("JKS"),
                        () -> assertThat(config.getProtocol()).isEqualTo("TLSv1.2"),
                        () -> assertThat(config.getSupportedProtocols()).isNull(),
                        () -> assertThat(config.isVerifyHostname()).isTrue(),
                        () -> assertThat(provider.getResolvedBy()).contains(
                                entry("keyStorePath", ResolvedBy.PROVIDER_DEFAULT),
                                entry("keyStorePassword", ResolvedBy.NONE),
                                entry("keyStoreType", ResolvedBy.PROVIDER_DEFAULT),
                                entry("trustStorePath", ResolvedBy.PROVIDER_DEFAULT),
                                entry("trustStorePassword", ResolvedBy.PROVIDER_DEFAULT),
                                entry("trustStoreType", ResolvedBy.PROVIDER_DEFAULT),
                                entry("verifyHostname", ResolvedBy.PROVIDER_DEFAULT),
                                entry("protocol", ResolvedBy.PROVIDER_DEFAULT),
                                entry("supportedProtocols", ResolvedBy.NONE)
                        )
                );
            }
        }

    }

    private void assertContextIsCorrect(TlsContextConfiguration config, ConfigProvider provider, ResolvedBy resolution) {
        assertAll(
                () -> assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH),
                () -> assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD),
                () -> assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE),
                () -> assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH),
                () -> assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD),
                () -> assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE),
                () -> assertThat(config.getProtocol()).isEqualTo(PROTOCOL),
                () -> assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY),
                () -> assertThat(config.isVerifyHostname()).isFalse(),
                () -> assertThat(config.isDisableSniHostCheck()).isTrue(),
                () -> assertThat(provider.getResolvedBy()).contains(
                        entry("keyStorePath", resolution),
                        entry("keyStorePassword", resolution),
                        entry("keyStoreType", resolution),
                        entry("trustStorePath", resolution),
                        entry("trustStorePassword", resolution),
                        entry("trustStoreType", resolution),
                        entry("verifyHostname", resolution),
                        entry("protocol", resolution),
                        entry("supportedProtocols", resolution)
                )
        );
    }
}
