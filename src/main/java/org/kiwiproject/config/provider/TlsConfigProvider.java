package org.kiwiproject.config.provider;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.TlsContextConfiguration;
import org.kiwiproject.config.provider.util.PropertyResolutionSettings;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Config provider that provides a {@link TlsContextConfiguration}.
 * <p>
 * Default resolution lookup keys can be found in the constants for this class
 * @see SinglePropertyResolver for resolution order
 */
@Slf4j
public class TlsConfigProvider implements ConfigProvider {

    private static final String SYSTEM_PROPERTY = "systemProperty";
    private static final String ENV_PROPERTY = "envVariable";
    private static final String EXTERNAL_PROPERTY = "externalProperty";

    private static final String KEYSTORE_PATH_FIELD = "keyStorePath";
    private static final String KEYSTORE_PASSWORD_FIELD = "keyStorePassword";
    private static final String KEYSTORE_TYPE_FIELD = "keyStoreType";
    private static final String TRUSTSTORE_PATH_FIELD = "trustStorePath";
    private static final String TRUSTSTORE_PASSWORD_FIELD = "trustStorePassword";
    private static final String TRUSTSTORE_TYPE_FIELD = "trustStoreType";
    private static final String VERIFY_HOSTNAME_FIELD = "verifyHostname";
    private static final String PROTOCOL_FIELD = "protocol";
    private static final String SUPPORTED_PROTOCOLS_FIELD = "supportedProtocols";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PATH_SYSTEM_PROPERTY = "kiwi.tls.keyStorePath";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PATH_ENV_VARIABLE = "KIWI_TLS_KEYSTORE_PATH";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PATH_EXTERNAL_PROPERTY_KEY = "tls.keyStorePath";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PASSWORD_SYSTEM_PROPERTY = "kiwi.tls.keyStorePassword";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PASSWORD_ENV_VARIABLE = "KIWI_TLS_KEYSTORE_PASSWORD";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY = "tls.keyStorePassword";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_TYPE_SYSTEM_PROPERTY = "kiwi.tls.keyStoreType";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_TYPE_ENV_VARIABLE = "KIWI_TLS_KEYSTORE_TYPE";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_TYPE_EXTERNAL_PROPERTY_KEY = "tls.keyStoreType";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PATH_SYSTEM_PROPERTY = "kiwi.tls.trustStorePath";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PATH_ENV_VARIABLE = "KIWI_TLS_TRUSTSTORE_PATH";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PATH_EXTERNAL_PROPERTY_KEY = "tls.trustStorePath";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY = "kiwi.tls.trustStorePassword";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PASSWORD_ENV_VARIABLE = "KIWI_TLS_TRUSTSTORE_PASSWORD";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY = "tls.trustStorePassword";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_TYPE_SYSTEM_PROPERTY = "kiwi.tls.trustStoreType";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_TYPE_ENV_VARIABLE = "KIWI_TLS_TRUSTSTORE_TYPE";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_TYPE_EXTERNAL_PROPERTY_KEY = "tls.trustStoreType";

    @VisibleForTesting
    static final String DEFAULT_VERIFY_HOSTNAME_SYSTEM_PROPERTY = "kiwi.tls.verifyHostname";

    @VisibleForTesting
    static final String DEFAULT_VERIFY_HOSTNAME_ENV_VARIABLE = "KIWI_TLS_VERIFY_HOSTNAME";

    @VisibleForTesting
    static final String DEFAULT_VERIFY_HOSTNAME_EXTERNAL_PROPERTY_KEY = "tls.verifyHostname";

    @VisibleForTesting
    static final String DEFAULT_PROTOCOL_SYSTEM_PROPERTY = "kiwi.tls.protocol";

    @VisibleForTesting
    static final String DEFAULT_PROTOCOL_ENV_VARIABLE = "KIWI_TLS_PROTOCOL";

    @VisibleForTesting
    static final String DEFAULT_PROTOCOL_EXTERNAL_PROPERTY_KEY = "tls.protocol";

    @VisibleForTesting
    static final String DEFAULT_SUPPORTED_PROTOCOLS_SYSTEM_PROPERTY = "kiwi.tls.supportedProtocols";

    @VisibleForTesting
    static final String DEFAULT_SUPPORTED_PROTOCOLS_ENV_VARIABLE = "KIWI_TLS_SUPPORTED_PROTOCOLS";

    @VisibleForTesting
    static final String DEFAULT_SUPPORTED_PROTOCOLS_EXTERNAL_PROPERTY_KEY = "tls.supportedProtocols";

    private static final Map<String, String> KEYSTORE_PATH_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_KEYSTORE_PATH_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_KEYSTORE_PATH_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_KEYSTORE_PATH_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> KEYSTORE_PASSWORD_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_KEYSTORE_PASSWORD_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_KEYSTORE_PASSWORD_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_KEYSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> KEYSTORE_TYPE_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_KEYSTORE_TYPE_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_KEYSTORE_TYPE_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_KEYSTORE_TYPE_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> TRUSTSTORE_PATH_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_TRUSTSTORE_PATH_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_TRUSTSTORE_PATH_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_TRUSTSTORE_PATH_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> TRUSTSTORE_PASSWORD_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_TRUSTSTORE_PASSWORD_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_TRUSTSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> TRUSTSTORE_TYPE_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_TRUSTSTORE_TYPE_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_TRUSTSTORE_TYPE_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_TRUSTSTORE_TYPE_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> VERIFY_HOSTNAME_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_VERIFY_HOSTNAME_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_VERIFY_HOSTNAME_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_VERIFY_HOSTNAME_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> PROTOCOL_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_PROTOCOL_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_PROTOCOL_ENV_VARIABLE, EXTERNAL_PROPERTY,
            DEFAULT_PROTOCOL_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> SUPPORTED_PROTOCOLS_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_SUPPORTED_PROTOCOLS_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_SUPPORTED_PROTOCOLS_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_SUPPORTED_PROTOCOLS_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, Map<String, String>> DEFAULTS_FOR_PROPERTIES = Map.of(
            KEYSTORE_PATH_FIELD, KEYSTORE_PATH_DEFAULTS,
            KEYSTORE_PASSWORD_FIELD, KEYSTORE_PASSWORD_DEFAULTS,
            KEYSTORE_TYPE_FIELD, KEYSTORE_TYPE_DEFAULTS,
            TRUSTSTORE_PATH_FIELD, TRUSTSTORE_PATH_DEFAULTS,
            TRUSTSTORE_PASSWORD_FIELD, TRUSTSTORE_PASSWORD_DEFAULTS,
            TRUSTSTORE_TYPE_FIELD, TRUSTSTORE_TYPE_DEFAULTS,
            VERIFY_HOSTNAME_FIELD, VERIFY_HOSTNAME_DEFAULTS,
            PROTOCOL_FIELD, PROTOCOL_DEFAULTS,
            SUPPORTED_PROTOCOLS_FIELD, SUPPORTED_PROTOCOLS_DEFAULTS
    );

    @Getter
    private final TlsContextConfiguration tlsContextConfiguration;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy keyStorePathResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy keyStorePasswordResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy keyStoreTypeResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy trustStorePathResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy trustStorePasswordResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy trustStoreTypeResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy verifyHostnameResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy protocolResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy supportedProtocolsResolvedBy;

    @SuppressWarnings("java:S107")
    @Builder
    private TlsConfigProvider(ExternalConfigProvider externalConfigProvider,
                              KiwiEnvironment kiwiEnvironment,
                              FieldResolverStrategy<String> keyStorePathResolverStrategy,
                              FieldResolverStrategy<String> keyStorePasswordResolverStrategy,
                              FieldResolverStrategy<String> keyStoreTypeResolverStrategy,
                              FieldResolverStrategy<String> trustStorePathResolverStrategy,
                              FieldResolverStrategy<String> trustStorePasswordResolverStrategy,
                              FieldResolverStrategy<String> trustStoreTypeResolverStrategy,
                              FieldResolverStrategy<Boolean> verifyHostnameResolverStrategy,
                              FieldResolverStrategy<String> protocolResolverStrategy,
                              FieldResolverStrategy<List<String>> supportedProtocolsResolverStrategy,
                              Supplier<TlsContextConfiguration> tlsContextConfigurationSupplier) {

        var originalConfiguration = getSuppliedConfigurationOrDefault(tlsContextConfigurationSupplier);

        tlsContextConfiguration = TlsContextConfiguration.builder()
                .protocol(resolveProperty(PROTOCOL_FIELD, protocolResolverStrategy, externalConfigProvider, kiwiEnvironment,
                        originalConfiguration.getProtocol(), this::setProtocolResolvedBy))
                .keyStorePath(resolveProperty(KEYSTORE_PATH_FIELD, keyStorePathResolverStrategy, externalConfigProvider, kiwiEnvironment,
                        originalConfiguration.getKeyStorePath(), this::setKeyStorePathResolvedBy))
                .keyStorePassword(resolveProperty(KEYSTORE_PASSWORD_FIELD, keyStorePasswordResolverStrategy, externalConfigProvider,
                        kiwiEnvironment, originalConfiguration.getKeyStorePassword(), this::setKeyStorePasswordResolvedBy))
                .keyStoreType(resolveProperty(KEYSTORE_TYPE_FIELD, keyStoreTypeResolverStrategy, externalConfigProvider, kiwiEnvironment,
                        originalConfiguration.getKeyStoreType(), this::setKeyStoreTypeResolvedBy))
                .trustStorePath(resolveProperty(TRUSTSTORE_PATH_FIELD, trustStorePathResolverStrategy, externalConfigProvider,
                        kiwiEnvironment, originalConfiguration.getTrustStorePath(), this::setTrustStorePathResolvedBy))
                .trustStorePassword(resolveProperty(TRUSTSTORE_PASSWORD_FIELD, trustStorePasswordResolverStrategy, externalConfigProvider,
                        kiwiEnvironment, originalConfiguration.getTrustStorePassword(), this::setTrustStorePasswordResolvedBy))
                .trustStoreType(resolveProperty(TRUSTSTORE_TYPE_FIELD, trustStoreTypeResolverStrategy, externalConfigProvider,
                        kiwiEnvironment, originalConfiguration.getTrustStoreType(), this::setTrustStoreTypeResolvedBy))
                .verifyHostname(resolveProperty(VERIFY_HOSTNAME_FIELD, verifyHostnameResolverStrategy, externalConfigProvider,
                        kiwiEnvironment, originalConfiguration.isVerifyHostname(), this::setVerifyHostnameResolvedBy, Boolean::parseBoolean))
                .supportedProtocols(resolveProperty(SUPPORTED_PROTOCOLS_FIELD, supportedProtocolsResolverStrategy, externalConfigProvider,
                        kiwiEnvironment, originalConfiguration.getSupportedProtocols(),
                        this::setSupportedProtocolsResolvedBy, str -> Arrays.asList(str.split(","))))
                .build();
    }

    private TlsContextConfiguration getSuppliedConfigurationOrDefault(Supplier<TlsContextConfiguration> tlsContextConfigurationSupplier) {
        setAllResolvedByToProviderDefault();

        if (nonNull(tlsContextConfigurationSupplier)) {
            return tlsContextConfigurationSupplier.get();
        }

        return TlsContextConfiguration.builder().build();
    }

    private String resolveProperty(String fieldName,
                                  FieldResolverStrategy<String> resolver,
                                  ExternalConfigProvider externalConfigProvider,
                                  KiwiEnvironment kiwiEnvironment,
                                  String originalValue,
                                  Consumer<ResolvedBy> resolvedBySetter) {

        return resolveProperty(fieldName, resolver, externalConfigProvider, kiwiEnvironment, originalValue, 
                resolvedBySetter, value -> value);
    }

    private <T> T resolveProperty(String fieldName,
                                  FieldResolverStrategy<T> resolver,
                                  ExternalConfigProvider externalConfigProvider,
                                  KiwiEnvironment kiwiEnvironment,
                                  T originalValue,
                                  Consumer<ResolvedBy> resolvedBySetter,
                                  Function<String, T> convertFromString) {

        var defaultFields = DEFAULTS_FOR_PROPERTIES.get(fieldName);

        var resolution = SinglePropertyResolver.resolveProperty(PropertyResolutionSettings.<T>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(resolver)
                .systemProperty(defaultFields.get(SYSTEM_PROPERTY))
                .environmentVariable(defaultFields.get(ENV_PROPERTY))
                .externalKey(defaultFields.get(EXTERNAL_PROPERTY))
                .defaultValue(originalValue)
                .convertFromString(convertFromString)
                .build());

        resolvedBySetter.accept(resolution.getResolvedBy());
        return resolution.getValue();
    }

    private void setAllResolvedByToProviderDefault() {
        keyStorePathResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        keyStorePasswordResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        keyStoreTypeResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        trustStorePathResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        trustStorePasswordResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        trustStoreTypeResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        verifyHostnameResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        protocolResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        supportedProtocolsResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(tlsContextConfiguration.getTrustStorePath()) &&
                isNotBlank(tlsContextConfiguration.getTrustStorePassword());
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of(
                KEYSTORE_PATH_FIELD, keyStorePathResolvedBy,
                KEYSTORE_PASSWORD_FIELD, keyStorePasswordResolvedBy,
                KEYSTORE_TYPE_FIELD, keyStoreTypeResolvedBy,
                TRUSTSTORE_PATH_FIELD, trustStorePathResolvedBy,
                TRUSTSTORE_PASSWORD_FIELD, trustStorePasswordResolvedBy,
                TRUSTSTORE_TYPE_FIELD, trustStoreTypeResolvedBy,
                VERIFY_HOSTNAME_FIELD, verifyHostnameResolvedBy,
                PROTOCOL_FIELD, protocolResolvedBy,
                SUPPORTED_PROTOCOLS_FIELD, supportedProtocolsResolvedBy
        );
    }
}
