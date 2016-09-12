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
import org.trustedanalytics.modelcatalog.h2omodelprovider.exceptions.NoSuchOfferingException;

import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ModelService {

  private static final String SERVICE = "h2o";
  private final CatalogOperations catalogOperations;
  private final LoadingCache<H2oInstanceCredentials, H2oInstance> h2oInstanceCache;

  @Autowired
  public ModelService(CatalogOperations catalogOperations,
                         LoadingCache<H2oInstanceCredentials, H2oInstance> h2oInstanceCache) {
    this.catalogOperations = catalogOperations;
    this.h2oInstanceCache = h2oInstanceCache;
  }

  @Scheduled(fixedDelayString = "${sync.delay_seconds:60}000")
  public void fetchModels() throws NoSuchOfferingException {
    Function<H2oInstanceCredentials, H2oInstance> loadH2oInstance = h2oInstanceCache::getUnchecked;

    //TODO: consider looking for running service
    Optional<H2oInstanceCredentials> h2oBroker = catalogOperations.fetchOfferings()
        .parallelStream().filter(x -> x.getName().equals(SERVICE)).findFirst();

    String offeringId = h2oBroker.map(H2oInstanceCredentials::getId)
        .orElseThrow(() -> new NoSuchOfferingException(SERVICE));

    System.out.println("fetchModels will be fired...");
    catalogOperations
            .fetchAllCredentials(offeringId)
            .stream()
            .map(loadH2oInstance)
            .flatMap(ModelsRetriever::takeOutAndMapModels)
            .collect(Collectors.toList())
            .forEach(x -> System.out.println(x.getId()));
  }
}
