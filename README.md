[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.vashilk/redis-cache/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.vashilk/redis-cache)  [![Code Grade](https://api.codiga.io/project/34996/status/svg)](https://app.codiga.io/project/34996/dashboard) [![Code Climate](https://codeclimate.com/github/cloudfoundry/membrane.png)](https://codeclimate.com/github/vashilK/Redis-cache) [![Known Vulnerabilities](https://snyk.io/test/github/vashilK/Redis-cache/badge.svg)](https://snyk.io/test/github/vashilK/Redis-cache)  [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![DOI](https://www.zenodo.org/badge/561818709.svg)](https://www.zenodo.org/badge/latestdoi/561818709)

# Redis-cache
Java client for redis to provide method-caching with cache synchronization feature based on Springboot's implementation of Redis with added functionality.

## What is Redis-cache for?
Redis-cache is a library built on spring-redis to provide and enhanced method caching feature to your application which works at method level by using 
the provided annotations.

## Getting Started
To get started with Jedis, first add it as a dependency in your Java project. If you're using Maven, that looks like this:

```xml
<dependency>
  <groupId>io.github.vashilk</groupId>
  <artifactId>redis-cache</artifactId>
  <version>1.0.4</version>
</dependency>
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
@CacheSave(group="group-name")
```
Any method annotated with the one above will be cached. Designed to be used on methods that return data, specifically for
heavily used ones with similar parameters.


```java
@CacheSync(group="group-name")
```
Methods annotated with the one above will trigger a synchronization of data for all methods
annotated with @CacheSave with the same 'group-name'. This annotation is designed to be used on methods
which modify the datasource from where you are querying but can be also used in events where you wish to 
manually trigger a synchronization.


## Contributing
This is a completely open source project, it is an idea that came up to me when using Redis in
one of my projects. All help and collaborations to refine and make this strong solution are welcome you can contact me at my [LinkedIn](https://mu.linkedin.com/in/neeschal-kissoon-03ab7516b).


