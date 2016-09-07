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
package org.trustedanalytics.modelcatalog.h2omodelprovider.service;

import org.trustedanalytics.modelcatalog.h2omodelprovider.client.CatalogOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelsRetriever;
import org.trustedanalytics.modelcatalog.rest.api.ModelMetadata;

import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ModelService {

  private static final String SERVICE = "h2o";
  private final String basicAuthCredentials;
  private final CatalogOperations catalogOperations;
  private final LoadingCache<H2oInstanceCredentials, H2oInstance> h2oInstanceCache;

  @Autowired
  public ModelService(String basicAuthCredentials, CatalogOperations catalogOperations,
                         LoadingCache<H2oInstanceCredentials, H2oInstance> h2oInstanceCache) {
    this.basicAuthCredentials = basicAuthCredentials;
    this.catalogOperations = catalogOperations;
    this.h2oInstanceCache = h2oInstanceCache;
  }

  public Collection<ModelMetadata> fetchModels() {
    Function<H2oInstanceCredentials, H2oInstance> loadH2oInstance = h2oInstanceCache::getUnchecked;
    Optional<H2oInstanceCredentials> h2oBroker = catalogOperations.fetchOfferings(basicAuthCredentials)
        .parallelStream().filter(x -> x.getName().equals(SERVICE)).findFirst();

    //TODO: cover all flow paths
    String offeringId = h2oBroker.get().getId();

    return catalogOperations
            .fetchAllCredentials(basicAuthCredentials, offeringId)
            .stream()
            .map(loadH2oInstance)
            .flatMap(ModelsRetriever::takeOutAndMapModels)
            .collect(Collectors.toList());
  }

}
