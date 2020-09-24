package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.clearAllSystemProperties;

import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.collect.KiwiMaps;
import org.kiwiproject.json.JsonHelper;

import java.nio.file.Path;
import java.util.Map;

@DisplayName("DropwizardDataSourceConfigProvider")
class DropwizardDataSourceConfigProviderTest {

    private static final String DRIVER_CLASS = "org.postgresql.Driver";
    private static final String URL = "jdbc://localhost:5432/test-db";
    private static final String USER = "kiwi";
    private static final String PASSWORD = "secret";
    private static final Map<String, String> ORM_PROPERTIES = KiwiMaps.newHashMap("prop", "value");

    private JsonHelper json;

    @BeforeEach
    void setUp() {
        json = new JsonHelper();
    }

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
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_DRIVER_CLASS_SYSTEM_PROPERTY, DRIVER_CLASS);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_URL_SYSTEM_PROPERTY, URL);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_USER_SYSTEM_PROPERTY, USER);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_PASSWORD_SYSTEM_PROPERTY, PASSWORD);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_MAX_SIZE_SYSTEM_PROPERTY, "1");
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_MIN_SIZE_SYSTEM_PROPERTY, "0");
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_INITIAL_SIZE_SYSTEM_PROPERTY, "0");
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_ORM_PROPERTIES_SYSTEM_PROPERTY, json.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();

                var config = provider.getDataSourceFactory();
                assertThat(config.getDriverClass()).isEqualTo(DRIVER_CLASS);
                assertThat(config.getUrl()).isEqualTo(URL);
                assertThat(config.getUser()).isEqualTo(USER);
                assertThat(config.getPassword()).isEqualTo(PASSWORD);
                assertThat(config.getMaxSize()).isEqualTo(1);
                assertThat(config.getMinSize()).isZero();
                assertThat(config.getInitialSize()).isZero();
                assertThat(config.getProperties()).containsAllEntriesOf(ORM_PROPERTIES);
                assertThat(provider.getResolvedBy()).contains(
                        entry("driverClass", ResolvedBy.SYSTEM_PROPERTY),
                        entry("url", ResolvedBy.SYSTEM_PROPERTY),
                        entry("user", ResolvedBy.SYSTEM_PROPERTY),
                        entry("password", ResolvedBy.SYSTEM_PROPERTY),
                        entry("maxSize", ResolvedBy.SYSTEM_PROPERTY),
                        entry("minSize", ResolvedBy.SYSTEM_PROPERTY),
                        entry("initialSize", ResolvedBy.SYSTEM_PROPERTY),
                        entry("ormProperties", ResolvedBy.SYSTEM_PROPERTY)
                );
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
//                addSystemProperty("a", STORE_PATH);
//                addSystemProperty("b", STORE_PASSWORD);
//                addSystemProperty("c", STORE_TYPE);
//                addSystemProperty("d", STORE_PATH);
//                addSystemProperty("e", STORE_PASSWORD);
//                addSystemProperty("f", STORE_TYPE);
//                addSystemProperty("g", "false");
//                addSystemProperty("h", PROTOCOL);
//                addSystemProperty("i", SUPPORTED_PROTOCOLS);
//
//                var provider = TlsConfigProvider.builder()
//                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("a").build())
//                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("b").build())
//                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("c").build())
//                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("d").build())
//                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("e").build())
//                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("f").build())
//                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().systemPropertyKey("g").build())
//                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().systemPropertyKey("h").build())
//                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().systemPropertyKey("i").build())
//                        .build();
//
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getProtocol()).isEqualTo(PROTOCOL);
//                assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY);
//                assertThat(config.isVerifyHostname()).isFalse();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("keyStorePassword", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("keyStoreType", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("trustStorePath", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("trustStorePassword", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("trustStoreType", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("verifyHostname", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("protocol", ResolvedBy.SYSTEM_PROPERTY),
//                        entry("supportedProtocols", ResolvedBy.SYSTEM_PROPERTY)
//                );
            }

        }

        @Nested
        class WithEnvironmentVariable {



            @Test
            void shouldBuildUsingDefaultEnvVariable() {
//                var env = mock(KiwiEnvironment.class);
//                when(env.getenv(TlsConfigProvider.DEFAULT_KEYSTORE_PATH_ENV_VARIABLE)).thenReturn(STORE_PATH);
//                when(env.getenv(TlsConfigProvider.DEFAULT_KEYSTORE_PASSWORD_ENV_VARIABLE)).thenReturn(STORE_PASSWORD);
//                when(env.getenv(TlsConfigProvider.DEFAULT_KEYSTORE_TYPE_ENV_VARIABLE)).thenReturn(STORE_TYPE);
//                when(env.getenv(TlsConfigProvider.DEFAULT_TRUSTSTORE_PATH_ENV_VARIABLE)).thenReturn(STORE_PATH);
//                when(env.getenv(TlsConfigProvider.DEFAULT_TRUSTSTORE_PASSWORD_ENV_VARIABLE)).thenReturn(STORE_PASSWORD);
//                when(env.getenv(TlsConfigProvider.DEFAULT_TRUSTSTORE_TYPE_ENV_VARIABLE)).thenReturn(STORE_TYPE);
//                when(env.getenv(TlsConfigProvider.DEFAULT_VERIFY_HOSTNAME_ENV_VARIABLE)).thenReturn("false");
//                when(env.getenv(TlsConfigProvider.DEFAULT_PROTOCOL_ENV_VARIABLE)).thenReturn(PROTOCOL);
//                when(env.getenv(TlsConfigProvider.DEFAULT_SUPPORTED_PROTOCOLS_ENV_VARIABLE)).thenReturn(SUPPORTED_PROTOCOLS);
//
//                var provider = TlsConfigProvider.builder()
//                        .kiwiEnvironment(env)
//                        .build();
//
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getProtocol()).isEqualTo(PROTOCOL);
//                assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY);
//                assertThat(config.isVerifyHostname()).isFalse();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.SYSTEM_ENV),
//                        entry("keyStorePassword", ResolvedBy.SYSTEM_ENV),
//                        entry("keyStoreType", ResolvedBy.SYSTEM_ENV),
//                        entry("trustStorePath", ResolvedBy.SYSTEM_ENV),
//                        entry("trustStorePassword", ResolvedBy.SYSTEM_ENV),
//                        entry("trustStoreType", ResolvedBy.SYSTEM_ENV),
//                        entry("verifyHostname", ResolvedBy.SYSTEM_ENV),
//                        entry("protocol", ResolvedBy.SYSTEM_ENV),
//                        entry("supportedProtocols", ResolvedBy.SYSTEM_ENV)
//                );
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
//                var env = mock(KiwiEnvironment.class);
//                when(env.getenv("a")).thenReturn(STORE_PATH);
//                when(env.getenv("b")).thenReturn(STORE_PASSWORD);
//                when(env.getenv("c")).thenReturn(STORE_TYPE);
//                when(env.getenv("d")).thenReturn(STORE_PATH);
//                when(env.getenv("e")).thenReturn(STORE_PASSWORD);
//                when(env.getenv("f")).thenReturn(STORE_TYPE);
//                when(env.getenv("g")).thenReturn("bar-host");
//                when(env.getenv("h")).thenReturn(PROTOCOL);
//                when(env.getenv("i")).thenReturn(SUPPORTED_PROTOCOLS);
//
//                var provider = TlsConfigProvider.builder()
//                        .kiwiEnvironment(env)
//                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("a").build())
//                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("b").build())
//                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("c").build())
//                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("d").build())
//                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("e").build())
//                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("f").build())
//                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().envVariable("g").build())
//                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().envVariable("h").build())
//                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().envVariable("i").build())
//                        .build();
//
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getProtocol()).isEqualTo(PROTOCOL);
//                assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY);
//                assertThat(config.isVerifyHostname()).isFalse();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.SYSTEM_ENV),
//                        entry("keyStorePassword", ResolvedBy.SYSTEM_ENV),
//                        entry("keyStoreType", ResolvedBy.SYSTEM_ENV),
//                        entry("trustStorePath", ResolvedBy.SYSTEM_ENV),
//                        entry("trustStorePassword", ResolvedBy.SYSTEM_ENV),
//                        entry("trustStoreType", ResolvedBy.SYSTEM_ENV),
//                        entry("verifyHostname", ResolvedBy.SYSTEM_ENV),
//                        entry("protocol", ResolvedBy.SYSTEM_ENV),
//                        entry("supportedProtocols", ResolvedBy.SYSTEM_ENV)
//                );
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
//                var provider = TlsConfigProvider.builder().externalConfigProvider(externalConfigProvider).build();
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getProtocol()).isEqualTo(PROTOCOL);
//                assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY);
//                assertThat(config.isVerifyHostname()).isFalse();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("keyStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("keyStoreType", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("trustStorePath", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("trustStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("trustStoreType", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("verifyHostname", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("protocol", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("supportedProtocols", ResolvedBy.EXTERNAL_PROPERTY)
//                );
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
//                var provider = TlsConfigProvider.builder()
//                        .externalConfigProvider(externalConfigProvider)
//                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("a").build())
//                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("b").build())
//                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("c").build())
//                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("d").build())
//                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("e").build())
//                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("f").build())
//                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().externalProperty("g").build())
//                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().externalProperty("h").build())
//                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().externalProperty("i").build())
//                        .build();
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getProtocol()).isEqualTo(PROTOCOL);
//                assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY);
//                assertThat(config.isVerifyHostname()).isFalse();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("keyStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("keyStoreType", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("trustStorePath", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("trustStorePassword", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("trustStoreType", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("verifyHostname", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("protocol", ResolvedBy.EXTERNAL_PROPERTY),
//                        entry("supportedProtocols", ResolvedBy.EXTERNAL_PROPERTY)
//                );
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
//                var provider = TlsConfigProvider.builder()
//                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue(STORE_PATH).build())
//                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue(STORE_PASSWORD).build())
//                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue(STORE_TYPE).build())
//                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue(STORE_PATH).build())
//                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue(STORE_PASSWORD).build())
//                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue(STORE_TYPE).build())
//                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().explicitValue(false).build())
//                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().explicitValue(PROTOCOL).build())
//                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder().explicitValue(
//                                List.of(SUPPORTED_PROTOCOLS_ARRAY)).build())
//                        .build();
//
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getProtocol()).isEqualTo(PROTOCOL);
//                assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY);
//                assertThat(config.isVerifyHostname()).isFalse();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.EXPLICIT_VALUE),
//                        entry("keyStorePassword", ResolvedBy.EXPLICIT_VALUE),
//                        entry("keyStoreType", ResolvedBy.EXPLICIT_VALUE),
//                        entry("trustStorePath", ResolvedBy.EXPLICIT_VALUE),
//                        entry("trustStorePassword", ResolvedBy.EXPLICIT_VALUE),
//                        entry("trustStoreType", ResolvedBy.EXPLICIT_VALUE),
//                        entry("verifyHostname", ResolvedBy.EXPLICIT_VALUE),
//                        entry("protocol", ResolvedBy.EXPLICIT_VALUE),
//                        entry("supportedProtocols", ResolvedBy.EXPLICIT_VALUE)
//                );
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
//                var provider = TlsConfigProvider.builder()
//                        .keyStorePathResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> STORE_PATH).build())
//                        .keyStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> STORE_PASSWORD).build())
//                        .keyStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> STORE_TYPE).build())
//                        .trustStorePathResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> STORE_PATH).build())
//                        .trustStorePasswordResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> STORE_PASSWORD).build())
//                        .trustStoreTypeResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> STORE_TYPE).build())
//                        .verifyHostnameResolverStrategy(FieldResolverStrategy.<Boolean>builder().valueSupplier(() -> false).build())
//                        .protocolResolverStrategy(FieldResolverStrategy.<String>builder().valueSupplier(() -> PROTOCOL).build())
//                        .supportedProtocolsResolverStrategy(FieldResolverStrategy.<List<String>>builder()
//                                .valueSupplier(() -> List.of(SUPPORTED_PROTOCOLS_ARRAY)).build())
//                        .build();
//
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getKeyStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getKeyStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getTrustStorePath()).isEqualTo(STORE_PATH);
//                assertThat(config.getTrustStorePassword()).isEqualTo(STORE_PASSWORD);
//                assertThat(config.getTrustStoreType()).isEqualTo(STORE_TYPE);
//                assertThat(config.getProtocol()).isEqualTo(PROTOCOL);
//                assertThat(config.getSupportedProtocols()).contains(SUPPORTED_PROTOCOLS_ARRAY);
//                assertThat(config.isVerifyHostname()).isFalse();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.SUPPLIER),
//                        entry("keyStorePassword", ResolvedBy.SUPPLIER),
//                        entry("keyStoreType", ResolvedBy.SUPPLIER),
//                        entry("trustStorePath", ResolvedBy.SUPPLIER),
//                        entry("trustStorePassword", ResolvedBy.SUPPLIER),
//                        entry("trustStoreType", ResolvedBy.SUPPLIER),
//                        entry("verifyHostname", ResolvedBy.SUPPLIER),
//                        entry("protocol", ResolvedBy.SUPPLIER),
//                        entry("supportedProtocols", ResolvedBy.SUPPLIER)
//                );
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCanProvide() {
//                var provider = TlsConfigProvider.builder().build();
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isBlank();
//                assertThat(config.getKeyStorePassword()).isBlank();
//                assertThat(config.getKeyStoreType()).isEqualTo("JKS");
//                assertThat(config.getTrustStorePath()).isBlank();
//                assertThat(config.getTrustStorePassword()).isBlank();
//                assertThat(config.getTrustStoreType()).isEqualTo("JKS");
//                assertThat(config.getProtocol()).isEqualTo("TLSv1.2");
//                assertThat(config.getSupportedProtocols()).isNull();
//                assertThat(config.isVerifyHostname()).isTrue();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.NONE),
//                        entry("keyStorePassword", ResolvedBy.NONE),
//                        entry("keyStoreType", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("trustStorePath", ResolvedBy.NONE),
//                        entry("trustStorePassword", ResolvedBy.NONE),
//                        entry("trustStoreType", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("verifyHostname", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("protocol", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("supportedProtocols", ResolvedBy.NONE)
//                );
            }

        }

        @Nested
        class WithProvidedTlsContext {

            @Test
            void shouldBuildUsingTheProvidedContextAsDefault() {
//                var context = TlsContextConfiguration.builder().keyStorePath("my-secret-key").build();
//
//                var provider = TlsConfigProvider.builder().tlsContextConfigurationSupplier(() -> context).build();
//
//                assertThat(provider.canProvide()).isTrue();
//                var config = provider.getTlsContextConfiguration();
//
//                assertThat(config.getKeyStorePath()).isEqualTo("my-secret-key");
//                assertThat(config.getKeyStorePassword()).isBlank();
//                assertThat(config.getKeyStoreType()).isEqualTo("JKS");
//                assertThat(config.getTrustStorePath()).isBlank();
//                assertThat(config.getTrustStorePassword()).isBlank();
//                assertThat(config.getTrustStoreType()).isEqualTo("JKS");
//                assertThat(config.getProtocol()).isEqualTo("TLSv1.2");
//                assertThat(config.getSupportedProtocols()).isNull();
//                assertThat(config.isVerifyHostname()).isTrue();
//                assertThat(provider.getResolvedBy()).contains(
//                        entry("keyStorePath", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("keyStorePassword", ResolvedBy.NONE),
//                        entry("keyStoreType", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("trustStorePath", ResolvedBy.NONE),
//                        entry("trustStorePassword", ResolvedBy.NONE),
//                        entry("trustStoreType", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("verifyHostname", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("protocol", ResolvedBy.PROVIDER_DEFAULT),
//                        entry("supportedProtocols", ResolvedBy.NONE)
//                );
            }
        }

    }
}
