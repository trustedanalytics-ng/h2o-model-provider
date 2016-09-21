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
package org.trustedanalytics.modelcatalog.h2omodelprovider.service;

import com.google.common.cache.LoadingCache;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.CatalogOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.DatabaseOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.InstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelFilter;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelMapper;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelsRetriever;
import org.trustedanalytics.modelcatalog.h2omodelprovider.exceptions.NoSuchOfferingException;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogWriterClient;

@Service
class ModelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

  private static final String SERVICE = "h2o";
  private final CatalogOperations catalogOperations;
  private final ModelCatalogWriterClient modelCatalogClient;
  private final LoadingCache<InstanceCredentials, H2oInstance> h2oInstanceCache;
  private final ModelFilter modelFilter;
  private final DatabaseOperations database;

  @Value("${services.catalog.core_organization_uuid}")
  private UUID coreOrganization;

  @Autowired
  public ModelService(
      CatalogOperations catalogOperations,
      LoadingCache<InstanceCredentials, H2oInstance> h2oInstanceCache,
      ModelCatalogWriterClient modelCatalogClient,
      ModelFilter modelFilter,
      DatabaseOperations database) {
    this.catalogOperations = catalogOperations;
    this.h2oInstanceCache = h2oInstanceCache;
    this.modelCatalogClient = modelCatalogClient;
    this.modelFilter = modelFilter;
    this.database = database;
  }

  @Scheduled(fixedDelayString = "${sync.delay_seconds:60}000")
  public void fetchModels() throws NoSuchOfferingException {
    Function<InstanceCredentials, H2oInstance> loadH2oInstance = h2oInstanceCache::getUnchecked;

    //TODO: consider looking for running service
    Optional<InstanceCredentials> h2oBroker =
        catalogOperations
            .fetchOfferings()
            .parallelStream()
            .filter(x -> x.getName().equals(SERVICE))
            .findFirst();

    String offeringId =
        h2oBroker
            .map(InstanceCredentials::getId)
            .orElseThrow(() -> new NoSuchOfferingException(SERVICE));

    LOGGER.info("fetchModels will be fired...");
    catalogOperations
        .fetchAllCredentials(offeringId)
        .stream()
        .map(loadH2oInstance)
        .flatMap(ModelsRetriever::pullOutModels)
        .filter(modelFilter)
        .parallel()
        .forEach(x -> pushToModelCatalog(x));
  }

  void pushToModelCatalog(H2oModel h2oModel) {
    ModelMapper mapper = new ModelMapper();
    modelCatalogClient.addModel(mapper.apply(h2oModel), coreOrganization);
    database.rememberModel(h2oModel);
  }
}
