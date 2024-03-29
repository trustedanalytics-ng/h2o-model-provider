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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.DatabaseOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.H2oSePublisherOperations;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModelId;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModels;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.InstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.Metadata;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogClientException;
import org.trustedanalytics.modelcatalog.rest.client.ModelCatalogWriterClient;
import org.trustedanalytics.modelcatalog.rest.entities.ModelDTO;
import org.trustedanalytics.modelcatalog.rest.entities.ModelModificationParametersDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, ITConfiguration.class})
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class H2oModelProviderIT {

  @Autowired private LoadingCache<InstanceCredentials, H2oInstance> h2oInstanceCache;

  @Autowired private ModelCatalogWriterClient modelCatalogClient;

  @Autowired private DatabaseOperations database;

  @Autowired private H2oSePublisherOperations h2oSePublisherClient;

  private ModelDTO fakeModel;

  @Before
  public void setUp() {
    reset(modelCatalogClient);
    reset(h2oSePublisherClient);

    UUID modelID = UUID.fromString("67cf91ce-5fad-4cf6-82c4-c0f932073035");
    fakeModel = new ModelDTO();
    fakeModel.setId(modelID);

    when(modelCatalogClient.addModel(any(), any())).thenReturn(fakeModel);
    when(h2oSePublisherClient.downloadEngine(any(), any())).thenReturn("fake jar".getBytes());
  }

  @Test
  public void shouldGetAndCacheModels() throws InterruptedException, ExecutionException {
    assertEquals(0, h2oInstanceCache.size());
    Thread.sleep(1500);

    InstanceCredentials instanceCredentials = new InstanceCredentials();
    instanceCredentials.setId("test-guid");

    InstanceCredentials instanceCredentialsNonexistent = new InstanceCredentials();
    instanceCredentialsNonexistent.setId("nonexistent-guid");

    assertEquals(1, h2oInstanceCache.size());
    assertEquals(null, h2oInstanceCache.getIfPresent(instanceCredentialsNonexistent));
    assertEquals(2, h2oInstanceCache.get(instanceCredentials).getModels().size());
  }

  @Test
  public void shouldPushMetadataToModelCatalogWhenModelMissing() throws InterruptedException {
    assertEquals(0, h2oInstanceCache.size());
    when(database.checkIfExists(any())).thenReturn(false);
    Thread.sleep(1500);

    verify(modelCatalogClient, atLeastOnce())
        .addModel(any(ModelModificationParametersDTO.class), any(String.class));
  }

  @Test
  public void shouldUploadJarFileOfModelWhenPushingModel() throws InterruptedException {
    assertEquals(0, h2oInstanceCache.size());
    when(database.checkIfExists(any())).thenReturn(false);
    Thread.sleep(1500);

    verify(h2oSePublisherClient, atLeastOnce()).downloadEngine(any(), any());
    verify(modelCatalogClient, atLeastOnce())
        .addArtifact(eq(fakeModel.getId()), any(), any(), eq("name of first model.jar"));
  }

  @Test
  public void shouldDeleteMetadataWhenArifactWasNotPushed() throws InterruptedException {

    assertEquals(0, h2oInstanceCache.size());
    when(database.checkIfExists(any())).thenReturn(false);
    when(modelCatalogClient.addModel(any(), any())).thenReturn(fakeModel);
    when(modelCatalogClient.addArtifact(eq(fakeModel.getId()), any(), any(), any()))
        .thenThrow(new ModelCatalogClientException("!"));
    Thread.sleep(1500);

    verify(modelCatalogClient, atLeastOnce()).deleteModel(fakeModel.getId());
  }
  
  @Test
  public void shouldNotPushArtifactWhenMetadataWasNotPushed() throws InterruptedException {

    assertEquals(0, h2oInstanceCache.size());
    when(database.checkIfExists(any())).thenReturn(false);
    when(modelCatalogClient.addModel(any(), any())).thenThrow(new ModelCatalogClientException("!"));
    Thread.sleep(1500);

    verify(modelCatalogClient, never()).addArtifact(any(), any(), any(), any());
  }

  @Test
  public void shouldNotDownloadJarFileOfModelAlreadyPushed() throws InterruptedException {
    assertEquals(0, h2oInstanceCache.size());
    when(database.checkIfExists(any())).thenReturn(true);
    Thread.sleep(1500);

    verifyZeroInteractions(h2oSePublisherClient);
  }

  @Test
  public void shouldNotSendAlreadyExistingDataToModelCatalog() throws InterruptedException {
    assertEquals(0, h2oInstanceCache.size());
    when(database.checkIfExists(any())).thenReturn(true);
    Thread.sleep(1500);

    verifyZeroInteractions(modelCatalogClient);
  }

  @RestController
  public static class H2oAndCatalogMockController {

    @Value("${server.port}")
    public String port;

    @RequestMapping(value = "/3/Models", method = RequestMethod.GET)
    public H2oModels getModels() {

      H2oModels h2oModels = new H2oModels();
      h2oModels.setModels(new ArrayList<>());

      H2oModelId firstId = new H2oModelId();
      firstId.setName("name of first model");
      H2oModel h2oModel = new H2oModel();
      h2oModel.setAlgorithmFullName("algorithmAbbreviation");
      h2oModel.setModelId(firstId);
      h2oModel.setTimestamp(123);

      H2oModelId secondId = new H2oModelId();
      secondId.setName("name of second model");
      H2oModel h2oModel2 = new H2oModel();
      h2oModel2.setAlgorithmFullName("algo2");
      h2oModel2.setModelId(secondId);
      h2oModel.setTimestamp(321);

      h2oModels.getModels().add(h2oModel);
      h2oModels.getModels().add(h2oModel2);
      return h2oModels;
    }

    @RequestMapping(value = "/api/v1/services", method = RequestMethod.GET)
    public Collection<InstanceCredentials> fetchOfferings() {
      Collection<InstanceCredentials> toReturn = new ArrayList<>();
      InstanceCredentials instance = new InstanceCredentials();
      instance.setId("h2o-guid");
      instance.setName("h2o-350");
      toReturn.add(instance);
      return toReturn;
    }

    @RequestMapping(value = "/api/v1/services/{serviceId}/instances", method = RequestMethod.GET)
    public Collection<InstanceCredentials> fetchAllCredentials() {
      Collection<InstanceCredentials> toReturn = new ArrayList<>();
      InstanceCredentials instance = new InstanceCredentials();
      instance.setId("test-guid");
      instance.setName("name");
      instance.setState("RUNNING");

      Collection<Metadata> metadata =
          Lists.newArrayList(
              new Metadata("login", "login"),
              new Metadata("password", "pass"),
              new Metadata("hostname", "http://localhost:" + port));

      instance.setMetadata(metadata);
      toReturn.add(instance);
      return toReturn;
    }
  }
}
