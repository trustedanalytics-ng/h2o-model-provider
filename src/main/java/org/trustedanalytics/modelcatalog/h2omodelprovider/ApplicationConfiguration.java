/*
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.modelcatalog.h2omodelprovider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import feign.Feign;
import feign.Feign.Builder;
import feign.Logger;
import feign.Request;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.CatalogOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.DatabaseOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.H2oClientsPool;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.OAuth2TokenProvider;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.RedisOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.InstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelFilter;
import org.trustedanalytics.modelcatalog.h2omodelprovider.service.H2oInstanceCacheLoader;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogClientBuilder;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogWriterClient;

@Configuration
@EnableScheduling
@EnableOAuth2Client
public class ApplicationConfiguration {

  @Value("${services.catalog.url}")
  private String catalogBaseUrl;

  @Value("${services.catalog.user}")
  private String catalogUser;

  @Value("${services.catalog.pass}")
  private String catalogPass;

  @Value("${services.model_catalog.url}")
  private String modelCatalogBaseUrl;

  @Value("${services.redis.url}")
  private String redisUrl;

  @Value("${services.redis.collection}")
  private String redisCollection;

  @Value("${maximum_cache_size}")
  private long maximumCacheSize;

  @Value("${cache_expiration_time_s}")
  private long cacheExpirationTimeS;

  @Bean
  public LoadingCache<InstanceCredentials, H2oInstance> buildH2oInstanceCache() {
    return CacheBuilder.newBuilder()
        .maximumSize(maximumCacheSize)
        .expireAfterWrite(cacheExpirationTimeS, TimeUnit.SECONDS)
        .build(new H2oInstanceCacheLoader(h2oClientsPool()));
  }

  @Bean
  public H2oClientsPool h2oClientsPool() {
    return new H2oClientsPool(clientSupplier());
  }

  @Bean
  public Supplier<Builder> clientSupplier() {
    return () ->
        Feign.builder()
            .encoder(new JacksonEncoder(objectMapper()))
            .decoder(new JacksonDecoder(objectMapper()))
            .logger(new Slf4jLogger(ApplicationConfiguration.class))
            .options(new Request.Options(30 * 1000, 10 * 1000))
            .logLevel(Logger.Level.BASIC);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .setPropertyNamingStrategy(new LowerCaseWithUnderscoresStrategy())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @Bean
  public CatalogOperations catalogOperations() {
    return clientSupplier()
        .get()
        .requestInterceptor(new BasicAuthRequestInterceptor(catalogUser, catalogPass))
        .target(CatalogOperations.class, catalogBaseUrl);
  }

  @Bean
  @ConfigurationProperties("spring.oauth2.client")
  public OAuth2ProtectedResourceDetails clientCredentials() {
    return new ClientCredentialsResourceDetails();
  }

  @Bean
  public ModelCatalogWriterClient modelCatalogClient(
      OAuth2ProtectedResourceDetails clientCredentials) {
    OAuth2TokenProvider provider = new OAuth2TokenProvider(clientCredentials);
    ModelCatalogClientBuilder builder = new ModelCatalogClientBuilder(modelCatalogBaseUrl);
    return builder.oAuthTokenProvider(provider).buildWriter();
  }

  @Bean
  @Lazy
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer().setAddress(redisUrl);
    return Redisson.create(config);
  }

  @Bean
  public DatabaseOperations database(RedissonClient redissonClient) {
    return new RedisOperations(redissonClient, redisCollection);
  }

  @Bean
  public ModelFilter modelFilter(DatabaseOperations database) {
    return new ModelFilter(database);
  }
}
