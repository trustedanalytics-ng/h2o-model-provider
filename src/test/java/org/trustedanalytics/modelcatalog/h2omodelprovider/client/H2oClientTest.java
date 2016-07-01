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
package org.trustedanalytics.modelcatalog.h2omodelprovider.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModels;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class H2oClientTest {

  @Mock
  private H2oOperations h2oOperations;
  @Mock
  private H2oInstanceCredentials h2oInstanceCredentials;

  @InjectMocks
  private H2oClient h2oClient;

  @Test
  public void shouldGetModelsUsingH2oOperations() {
    // given
    final List<H2oModel> models = prepareModels();
    // when
    H2oInstance h2oInstance = h2oClient.fetchH2oInstance();
    // then
    assertThat(h2oInstance.getModels()).containsAll(models);
  }

  @Test
  public void shouldSetInstanceCredentials() {
    // given
    prepareModels();
    // when
    H2oInstance h2oInstance = h2oClient.fetchH2oInstance();
    // then
    assertThat(h2oInstance.getInstanceCredentials()).isSameAs(h2oInstanceCredentials);
  }

  private List<H2oModel> prepareModels() {
    H2oModels h2oModels = new H2oModels();
    final List<H2oModel> models = new ArrayList<>();
    h2oModels.setModels(models);
    when(h2oOperations.fetchModels()).thenReturn(h2oModels);
    return models;
  }
}
