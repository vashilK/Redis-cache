[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.vashilk/redis-cache/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.vashilk/redis-cache) [![Code Climate](https://codeclimate.com/github/cloudfoundry/membrane.png)](https://codeclimate.com/github/vashilK/Redis-cache) [![Known Vulnerabilities](https://snyk.io/test/github/vashilK/Redis-cache/badge.svg)](https://snyk.io/test/github/vashilK/Redis-cache)  [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![DOI](https://www.zenodo.org/badge/561818709.svg)](https://www.zenodo.org/badge/latestdoi/561818709)

# Redis-cache
Java client for redis to provide method-caching with cache synchronization feature based on Springboot's implementation of Redis with added functionality.

## What is Redis-cache for?

Redis-cache is a library built on spring-redis to provide an enhanced method caching feature to your application which
works at method level by using
the provided annotations.

## Getting Started

To get started with Redis-Cache, first add it as a dependency in your Java project. If you're using Maven, that looks
like
this:

```xml

<dependency>
    <groupId>io.github.vashilk</groupId>
    <artifactId>redis-cache</artifactId>
    <version>1.0.5.1</version>
</dependency>
```

For Gradle:

```kotlin
implementation("io.github.vashilk:redis-cache:1.0.5.1")
```

Next you will need to connect to your Redis instance

```java

@Configuration
public class RedisConfig {

    @Bean
    public JedisConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("localhost");
        configuration.setPort(6379);
        return new JedisConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new JdkSerializationRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

}
```

You can use your own connection to have access to a Redis-cluster with multiple redis-instances but
make sure the implementation beneath uses RedisTemplate for the annotation to work properly.

Next you will use custom annotations:

```java
@CacheSave(group = "group-name")
```

Any method annotated with the one above will be cached. Designed to be used on methods that return data, specifically
for
heavily used ones with similar parameters.

```java
@CacheSync(group = "group-name")
```

Methods annotated with the one above will trigger a synchronization of data for all methods
annotated with @CacheSave with the same 'group-name'. This annotation is designed to be used on methods
which modify the datasource from where you are querying but can be also used in events where you wish to
manually trigger a synchronization.

```java
@CacheRelease(group = "group-name")
```

Works as a cache eviction, any method annotated with this will clear a whole group cached with the same group-name
before the method runs. With this everytime the method is run it will push new data to the cache.

###

### Model

This tool serializes and deserializes models you use with your data-source, in order for it to work properly you need to
annotate the models you wish to be cached with:

```java
@RedisCacheSerializable
```

To generate the models that this tool requires please add this plugin to your pom and use this command:

pom.xml

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.0.0</version>
            <configuration>
                <mainClass>org.nki.redis.cache.generator.ModelGenerator</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

command

```shell
mvn clean install exec:java
```

###

### Aspect Trigger

Since this project uses AspectJ to perform actions you will need to add pre-bundled aspects from this repository to
your project using:

```xml

<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.14.0</version>
    <configuration>
        <showWeaveInfo>true</showWeaveInfo>
        <source>11</source>
        <target>11</target>
        <Xlint>ignore</Xlint>
        <complianceLevel>11</complianceLevel>
        <encoding>UTF-8</encoding>
        <verbose>true</verbose>
        <excludes>
            <exclude>**/*.java</exclude>
        </excludes>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>com.testing</groupId>
                <artifactId>redis-cache</artifactId>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
    <executions>
        <execution>
            <phase>process-sources</phase>
            <goals>
                <goal>compile</goal>
                <goal>test-compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Prerequisites

You require AspectJ for this repository to work:

```xml

<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
</dependency>

<dependency>
<groupId>org.aspectj</groupId>
<artifactId>aspectjrt</artifactId>
<version>1.9.7</version>
<scope>runtime</scope>
</dependency>
```

## Compilation issues

If your code includes Lombok as a dependecy and you experience any build issues with your code after adding the
dependencies and plugins in
intelliJ idea before deployment do the following:

Go to Project Structure, Modules, select AspectJ and check Post-compile weave mode.
This will ensure ajc runs after Lombok and can see the generated classes.

## Contributing

This is a completely open source project, it is an idea that came up to me when using Redis in
one of my projects. All help and collaborations to refine and make this strong solution are welcome you can contact me
at my [LinkedIn](https://mu.linkedin.com/in/neeschal-kissoon-03ab7516b).


