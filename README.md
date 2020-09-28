### Dropwizard Config Providers
[![Build Status](https://travis-ci.com/kiwiproject/dropwizard-config-providers.svg?branch=master)](https://travis-ci.com/kiwiproject/dropwizard-config-providers)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kiwiproject_dropwizard-config-providers&metric=alert_status)](https://sonarcloud.io/dashboard?id=kiwiproject_dropwizard-config-providers)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kiwiproject_dropwizard-config-providers&metric=coverage)](https://sonarcloud.io/dashboard?id=kiwiproject_dropwizard-config-providers)
[![javadoc](https://javadoc.io/badge2/org.kiwiproject/dropwizard-config-providers/javadoc.svg)](https://javadoc.io/doc/org.kiwiproject/dropwizard-config-providers)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/org.kiwiproject/dropwizard-config-providers)](https://search.maven.org/search?q=g:org.kiwiproject%20a:dropwizard-config-providers)

Utility library to assist in providing default config properties to dropwizard services

#### How to use it
* Add the Maven dependency (available in Maven Central)

```xml
<dependency>
    <groupId>org.kiwiproject</groupId>
    <artifactId>dropwizard-config-providers</artifactId>
    <version>0.1.0</version>
</dependency>
```

* Add the following to your Configuration class:

```java
public class YourConfiguration extends Configuration {

    private String serviceName = ServiceIdentityProvider.builder().build().getName();

}
```

#### Property resolution order of precedence

The service providers will resolve properties in the following order:
1. System property with the given system property key
2. System property with the default system property key (See the specific provider for details)
3. Environment variable with the given variable name
4. Environment variable with the default variable name (See the specific provider for details)
5. An explicit value
6. The value from an external configuration file with the given key (See ExternalPropertyProvider for details)
7. The value from an external configuration file with the default key (See the specific provider for details)
8. The value from a given supplier

#### Current Providers

The following providers are currently available in this library.

| Provider | Description | Properties Resolved |
| -------- | ----------- | ------------------- |
| ActiveMQConfigProvider | Resolves the connection string for ActiveMQ servers | activeMQServers |
| DropwizardDataSourceConfigProvider | Resolves a DataSourceFactory for connecting to a relational database in Dropwizard | dataSourceFactory |
| ElkLoggerConfigProvider | Resolves the connection and configuration information for an ELK Logger server | host, port, customFields |
| ElucidationConfigProvider | Resolves the connection and configuration for an Elucidation server | host, port, enabled |
| HibernateConfigProvider | Resolves default properties for a Hibernate connection | hibernateProperties |
| MongoConfigProvider | Resolves the connection string for Mongo servers | url |
| NetworkIdentityConfigProvider | Resolves a named network, useful if you want to have services run in different subnets/VPCs | network |
| ServiceIdentityConfigProvider | Resolves identity information for a running service | name, version, environment (deployed) |
| TlsConfigProvider | Resolves the properties to use in a TLS Configuration | tlsContextConfiguration |
| SharedStorageConfigProvider | Resolves a directory path that will be used for shared storage between services | sharedStoragePath |
| ZooKeeperConfigProvider | Resolves the ZooKeeper connection string | connectString |

#### Custom Providers

Custom providers can be created by extending the `ConfigProvider` interface.  Also, the existing providers can be extended by setting various lookup mechanisms
listed in the order of precedence section.