package io.keepup.cms.core.cache;

import io.keepup.cms.core.persistence.Content;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.spring.SpringCacheManager;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains configurations for different cache types.
 */
@Configuration
@EnableCaching
public class KeepupCacheConfiguration {

    public static final String CONTENT_CACHE_NAME = "content";

    @Profile("dev")
    @Bean("cacheManager")
    public CacheManager simpleCacheManager() {
        var cacheManager = new SimpleCacheManager();
        var contentCache = new ConcurrentMapCache(CONTENT_CACHE_NAME, false);
        cacheManager.setCaches(Collections.singletonList(contentCache));
        return cacheManager;
    }

    @Profile("ignite")
    @Bean("cacheManager")
    public CacheManager igniteCacheManager() {
        var cacheManager = new SpringCacheManager();
        cacheManager.setConfiguration(igniteConfiguration());
        return cacheManager;
    }

    @Profile("ignite")
    @Bean(name = "igniteConfiguration")
    public IgniteConfiguration igniteConfiguration() {
        var igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setIgniteInstanceName("keepupGrid");
        igniteConfiguration.setPeerClassLoadingEnabled(true);
        igniteConfiguration.setLocalHost("127.0.0.1");

        var tcpDiscoverySpi = new TcpDiscoverySpi();
        var ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        tcpDiscoverySpi.setIpFinder(ipFinder);
        tcpDiscoverySpi.setLocalPort(47500);
        tcpDiscoverySpi.setLocalPortRange(9);
        igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);

        var communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setLocalAddress("localhost");
        communicationSpi.setLocalPort(48100);
        communicationSpi.setSlowClientQueueLimit(1000);
        igniteConfiguration.setCommunicationSpi(communicationSpi);


        igniteConfiguration.setCacheConfiguration(cacheConfiguration());

        return igniteConfiguration;

    }

    @Profile("ignite")
    @Bean(name = "igniteCacheConfiguration")
    public CacheConfiguration<Long, Content>[] cacheConfiguration() {
        List<CacheConfiguration<Long, Content>> cacheConfigurations = new ArrayList<>();
        var cacheConfiguration = new CacheConfiguration<Long, Content>();
        cacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cacheConfiguration.setCacheMode(CacheMode.REPLICATED);
        cacheConfiguration.setName(CONTENT_CACHE_NAME);
        cacheConfiguration.setWriteThrough(false);
        cacheConfiguration.setReadThrough(false);
        cacheConfiguration.setWriteBehindEnabled(false);
        cacheConfiguration.setBackups(1);
        cacheConfiguration.setStatisticsEnabled(true);

        cacheConfigurations.add(cacheConfiguration);

        return cacheConfigurations.toArray(new CacheConfiguration[1]);
    }
}
