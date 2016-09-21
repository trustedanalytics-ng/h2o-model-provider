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
import org.trustedanalytics.modelcatalog.rest.entities.ModelModificationParametersDTO;

public class ModelMapperTest {

  private static final String MODEL_NAME = "model name";
  private static final String ALGORITHM_NAME = "xyz";
  private static final String ALGORITHM_FULL_NAME = "algorithm full name";

  private ModelMapper modelMapper;
  private H2oModel h2oModel;

  @Before
  public void setUp() {
    modelMapper = new ModelMapper();
    h2oModel = createExemplaryH2oModel();
  }

  @Test
  public void shouldCopyModelName() {
    // when
    ModelModificationParametersDTO modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getName()).isEqualTo(MODEL_NAME);
  }

  @Test
  public void shouldTakeShortAlgorithmNameIfFullNameIsNull() {
    // given
    h2oModel.setAlgorithmFullName(null);
    // when
    ModelModificationParametersDTO modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getAlgorithm()).isEqualTo(ALGORITHM_NAME);
  }

  @Test
  public void shouldTakeShortAlgorithmNameIfFullNameIsEmpty() {
    // given
    h2oModel.setAlgorithmFullName("");
    // when
    ModelModificationParametersDTO modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getAlgorithm()).isEqualTo(ALGORITHM_NAME);
  }

  @Test
  public void shouldConstructDescriptionOutOfModelNameAndAlgorithm() {
    // when
    ModelModificationParametersDTO modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getDescription()).contains(MODEL_NAME, ALGORITHM_FULL_NAME);
  }

  private H2oModel createExemplaryH2oModel() {
    H2oModel model = new H2oModel();
    H2oModelId modelId = new H2oModelId();
    modelId.setName(MODEL_NAME);
    model.setModelId(modelId);
    model.setAlgorithmAbbreviation(ALGORITHM_NAME);
    model.setAlgorithmFullName(ALGORITHM_FULL_NAME);
    return model;
  }
}
