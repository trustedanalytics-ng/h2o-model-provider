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

import static org.junit.Assert.assertEquals;

import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModelId;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModels;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.Metadata;

import com.google.common.cache.LoadingCache;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, ITConfiguration.class})
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class ModelCatalogIT {

  @Autowired
  private H2oInstancesOperations h2oInstancesOperations;

  @Autowired
  private LoadingCache<H2oInstanceCredentials, H2oInstance> h2oInstanceCache;

  @Test
  public void shouldGetAndCacheModels() throws ExecutionException {

    assertEquals(0, h2oInstanceCache.size());

    h2oInstancesOperations.getAnalyticsToolInstances(UUID.randomUUID());

    H2oInstanceCredentials h2oInstanceCredentials = new H2oInstanceCredentials();
    h2oInstanceCredentials.setId("test-guid");

    H2oInstanceCredentials h2oInstanceCredentialsNonexistent = new H2oInstanceCredentials();
    h2oInstanceCredentialsNonexistent.setId("nonexistent-guid");

    assertEquals(1, h2oInstanceCache.size());
    assertEquals(null, h2oInstanceCache.getIfPresent(h2oInstanceCredentialsNonexistent));
    assertEquals(2, h2oInstanceCache.get(h2oInstanceCredentials).getModels().size());
  }

  @RestController
  public static class TestController {

    @Value("${server.port}")
    public String port;

    @RequestMapping(value = "/3/Models", method = RequestMethod.GET)
    public H2oModels getModels() {

      H2oModels h2oModels = new H2oModels();
      h2oModels.setModels(new ArrayList<>());

      H2oModel h2oModel = new H2oModel();
      h2oModel.setAlgorithmFullName("algorithmAbbreviation");
      h2oModel.setModelId(new H2oModelId());

      H2oModel h2oModel2 = new H2oModel();
      h2oModel2.setAlgorithmFullName("algo2");
      h2oModel2.setModelId(new H2oModelId());

      h2oModels.getModels().add(h2oModel);
      h2oModels.getModels().add(h2oModel2);
      return h2oModels;
    }

    @RequestMapping(value = "/api/v1/services", method = RequestMethod.GET)
    public Collection<H2oInstanceCredentials> fetchOfferings() {
      Collection<H2oInstanceCredentials> toReturn = new ArrayList<>();
      H2oInstanceCredentials instance = new H2oInstanceCredentials();
      instance.setId("h2o-guid");
      instance.setName("h2o");
      toReturn.add(instance);
      return toReturn;
    }

    @RequestMapping(value = "/api/v1/services/{serviceId}/instances", method = RequestMethod.GET)
    public Collection<H2oInstanceCredentials> fetchAllCredentials(@PathVariable String serviceId) {
      Collection<H2oInstanceCredentials> toReturn = new ArrayList<>();
      H2oInstanceCredentials instance = new H2oInstanceCredentials();
      instance.setId("test-guid");
      instance.setName("name");

      Collection<Metadata> metadata = new ArrayList<Metadata>() {{
        add(new Metadata("login", "login"));
        add(new Metadata("password", "pass"));
        add(new Metadata("hostname", "localhost:" + port));
      }};

      instance.setMetadata(metadata);
      toReturn.add(instance);
      return toReturn;
    }
  }
}
