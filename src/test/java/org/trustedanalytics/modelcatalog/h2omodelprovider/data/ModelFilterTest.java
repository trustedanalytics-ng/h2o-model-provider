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

package org.trustedanalytics.modelcatalog.h2omodelprovider.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.RedisOperations;

@RunWith(MockitoJUnitRunner.class)
public class ModelFilterTest {

  @Mock RedisOperations db;

  ModelFilter modelFilter;

  @Before
  public void setUp() {
    modelFilter = new ModelFilter(db);
  }

  @Test
  public void shouldNotPassIncompleteModel() {
    //given
    H2oModel model = prepareModelWithoutCompletenessTimestamp();

    // when
    boolean result = modelFilter.test(model);

    // then
    assertThat(result).isFalse();
  }

  public H2oModel prepareModelWithoutCompletenessTimestamp() {
    H2oModelId modelId = new H2oModelId();
    modelId.setName("anything");
    H2oModel model = new H2oModel();
    model.setTimestamp(0);
    model.setModelId(modelId);
    return model;
  }
}
