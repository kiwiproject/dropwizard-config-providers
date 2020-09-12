package org.kiwiproject.config;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotBlank;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.jar.Jars;

@Getter
@Slf4j
public class ServiceIdentityProvider implements ConfigProvider {

    private String name;
    private ResolvedBy nameResolvedBy;

    private String version;
    private ResolvedBy versionResolvedBy;

    private String environment;
    private ResolvedBy environmentResolvedBy;

    public ServiceIdentityProvider() {
        // TODO: When kiwi 0.10.0 is released, rename this to KiwiJars
        var path = Jars.getPath(this.getClass());
        path.ifPresent(val -> resolveIdentity(val, new ExternalPropertyProvider()));
    }

    public ServiceIdentityProvider(String path) {
        this(path, new ExternalPropertyProvider());
    }

    public ServiceIdentityProvider(String path, ExternalPropertyProvider propertyProvider) {
        resolveIdentity(path, propertyProvider);
    }

    public ServiceIdentityProvider(String name, String version, String environment) {
        this.name = requireNotBlank(name);
        this.nameResolvedBy = ResolvedBy.EXPLICIT_VALUE;
        this.version = requireNotBlank(version);
        this.versionResolvedBy = ResolvedBy.EXPLICIT_VALUE;
        this.environment = requireNotBlank(environment);
        this.environmentResolvedBy = ResolvedBy.EXPLICIT_VALUE;
    }

    @Override
    public boolean canProvide() {
        return isNotBlank(name) && isNotBlank(version) && isNotBlank(environment);
    }

    protected void resolveIdentity(String path, ExternalPropertyProvider externalProvider) {
        initializeResolutionsToNone();
//        resolveIdentityFromSystemProperties()
    }

    private void initializeResolutionsToNone() {
        nameResolvedBy = ResolvedBy.NONE;
        versionResolvedBy = ResolvedBy.NONE;
        environmentResolvedBy = ResolvedBy.NONE;
    }
}
