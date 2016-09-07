/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.modelcatalog.h2omodelprovider;

import org.trustedanalytics.modelcatalog.h2omodelprovider.client.CatalogOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.H2oClientsPool;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.Instance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.security.OAuth2TokenExtractor;
import org.trustedanalytics.modelcatalog.h2omodelprovider.service.H2oInstanceCacheLoader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import feign.Feign;
import feign.Feign.Builder;
import feign.Logger;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

@Configuration
public class ApplicationConfiguration {

  @Value("${services.catalog}")
  private String catalogBaseUrl;

  @Value("${services.catalogUser")
  private String catalogUser;

  @Value("${services.catalogPass")
  private String catalogPass;

  @Value("${maximum_cache_size}")
  private long maximumCacheSize;

  @Value("${cache_expiration_time_s}")
  private long cacheExpirationTimeS;

  @Bean
  public LoadingCache<Instance, H2oInstance> buildH2oInstanceCache() {
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
  public Function<Authentication, String> tokenExtractor() {
    return new OAuth2TokenExtractor();
  }

  @Bean
  public Supplier<Builder> clientSupplier() {
    return () -> Feign.builder()
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
    return clientSupplier().get().target(CatalogOperations.class, catalogBaseUrl);
  }

  @Bean
  public String basicAuthCredentials() {
    return Base64.encodeBase64String((catalogUser + ":" + catalogPass).getBytes(Charsets.UTF_8));
  }
}
