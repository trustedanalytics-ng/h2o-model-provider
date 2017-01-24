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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.CatalogOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.DatabaseOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.H2oSePublisherOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.InstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.MetadataUrlEncoder;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelFilter;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelMapper;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.ModelsRetriever;
import org.trustedanalytics.modelcatalog.h2omodelprovider.exceptions.NoSuchOfferingException;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogClientException;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogWriterClient;
import org.trustedanalytics.modelcatalog.rest.entities.ArtifactActionDTO;
import org.trustedanalytics.modelcatalog.rest.entities.ModelDTO;

@Service
class ModelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

  private static final String SERVICE = "h2o-350";
  private static final String EXPECTED_STATE = "RUNNING";
  private static final String ARTIFACT_EXTENSION = ".jar";
  private final CatalogOperations catalogOperations;
  private final ModelCatalogWriterClient modelCatalogClient;
  private final LoadingCache<InstanceCredentials, H2oInstance> h2oInstanceCache;
  private final ModelFilter modelFilter;
  private final DatabaseOperations database;
  private final H2oSePublisherOperations h2oSePublisherClient;
  private static Set<ArtifactActionDTO> defaultActions;

  @Value("${services.catalog.core_organization_uuid}")
  private String coreOrganization;

  @Autowired
  public ModelService(
      CatalogOperations catalogOperations,
      LoadingCache<InstanceCredentials, H2oInstance> h2oInstanceCache,
      ModelCatalogWriterClient modelCatalogClient,
      ModelFilter modelFilter,
      DatabaseOperations database,
      H2oSePublisherOperations h2oSePublisherClient) {
    this.catalogOperations = catalogOperations;
    this.h2oInstanceCache = h2oInstanceCache;
    this.modelCatalogClient = modelCatalogClient;
    this.modelFilter = modelFilter;
    this.database = database;
    this.h2oSePublisherClient = h2oSePublisherClient;
    defaultActions = new HashSet<>();
    defaultActions.add(ArtifactActionDTO.PUBLISH_JAR_SCORING_ENGINE);
  }

  @Scheduled(fixedDelayString = "${sync.delay_seconds:60}000")
  public void fetchModels() throws NoSuchOfferingException {
    Function<InstanceCredentials, H2oInstance> loadH2oInstance = h2oInstanceCache::getUnchecked;

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

    LOGGER.info("Syncing informations about h2o models in the environment with model-catalog...");
    catalogOperations
        .fetchAllCredentials(offeringId)
        .stream()
        .filter(x -> x.getState().equals(EXPECTED_STATE))
        .map(loadH2oInstance)
        .flatMap(ModelsRetriever::pullOutModels)
        .filter(modelFilter)
        .parallel()
        .forEach(this::pushToModelCatalog);
  }

  void pushToModelCatalog(H2oModel h2oModel) {
    // TODO: rewrite the whole method after implementation of DPNG-11029
    ModelMapper mapper = new ModelMapper();

    byte[] jar =
        h2oSePublisherClient.downloadEngine(
            MetadataUrlEncoder.encode(h2oModel), h2oModel.getModelId().getName());
    String fileName = h2oModel.getModelId().getName() + ARTIFACT_EXTENSION;
    InputStream stream = new ByteArrayInputStream(jar);
    LOGGER.debug("Size of prepared JAR artifact: " + jar.length);

    ModelDTO added;
    LOGGER.debug("Pushing model metadata.");
    try {
      added = modelCatalogClient.addModel(mapper.apply(h2oModel), coreOrganization);
    } catch (ModelCatalogClientException ex) {
      LOGGER.error("Wasn't able to push metadata to model-catalog!", ex);
      return;
    }

    LOGGER.debug("Uploading artifact to model-catalog: " + fileName);
    try {
      modelCatalogClient.addArtifact(added.getId(), defaultActions, stream, fileName);
    } catch (ModelCatalogClientException ex) {
      LOGGER.error("Wasn't able to upload model artifact to model-catalog!", ex);
      LOGGER.debug("Fallback! Deleting metadata... We will try again soon.");
      try {
        modelCatalogClient.deleteModel(added.getId());
      } catch (ModelCatalogClientException e) {
        LOGGER.warn("Wasn't able to delete model metadata after failed artifact upload.", e);
      }
      return;
    }
    LOGGER.debug("Persising informations about pushed model in internal DB");
    database.rememberModel(h2oModel);
  }
}
