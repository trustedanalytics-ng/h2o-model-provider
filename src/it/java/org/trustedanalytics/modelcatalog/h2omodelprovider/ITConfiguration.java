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

import static org.mockito.Mockito.mock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.DatabaseOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.H2oSePublisherOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.RedisOperations;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogWriterClient;

@Configuration
@Profile("test")
@EnableScheduling
class ITConfiguration {

  @Value("${services.catalog.url}")
  public String catalogUrl;

  @Bean
  public ModelCatalogWriterClient modelCatalogClient() {
    return mock(ModelCatalogWriterClient.class);
  }

  @Bean
  public H2oSePublisherOperations h2oSePublisherClient() {
    return mock(H2oSePublisherOperations.class);
  }

  @Bean
  public DatabaseOperations database() {
    return mock(RedisOperations.class);
  }
}
