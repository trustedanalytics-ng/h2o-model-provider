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
package org.trustedanalytics.modelcatalog.h2omodelprovider.data;

import org.junit.Before;
import org.junit.Test;
import org.trustedanalytics.modelcatalog.rest.api.ModelMetadata;
import org.trustedanalytics.modelcatalog.rest.api.ModelStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelMapperTest {

  private static final String H2O_SERVER_ID = "13";
  private static final String MODEL_NAME = "model name";
  private static final String ALGORITHM_NAME = "xyz";
  private static final String ALGORITHM_FULL_NAME = "algorithm full name";

  private static final String DEFAULT_FORMAT = "h2o";
  private static final ModelStatus DEFAULT_STATUS = ModelStatus.DRAFT;
  private static final String DEFAULT_OWNER_ID = "h2o user";

  private ModelMapper modelMapper;
  private H2oModel h2oModel;

  @Before
  public void setUp() {
    modelMapper = new ModelMapper(H2O_SERVER_ID);
    h2oModel = createExamplaryH2oModel();
  }

  @Test
  public void shouldConstructModelIdOutOfServerIdAndModelName() {
    // when
    ModelMetadata modelMetadata = modelMapper.apply(h2oModel);
    //then
    assertThat(modelMetadata.getId()).contains(H2O_SERVER_ID, MODEL_NAME);
  }

  @Test
  public void shouldCopyModelName() {
    // when
    ModelMetadata modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getName()).isEqualTo(MODEL_NAME);
  }

  @Test
  public void shouldTakeShortAlgorithmNameIfFullNameIsNull() {
    // given
    h2oModel.setAlgorithmFullName(null);
    // when
    ModelMetadata modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getAlgorithm()).isEqualTo(ALGORITHM_NAME);

  }

  @Test
  public void shouldTakeShortAlgorithmNameIfFullNameIsEmpty() {
    // given
    h2oModel.setAlgorithmFullName("");
    // when
    ModelMetadata modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getAlgorithm()).isEqualTo(ALGORITHM_NAME);
  }

  @Test
  public void shouldConstructDescriptionOutOfModelNameAndAlgorithm() {
    // when
    ModelMetadata modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getDescription()).contains(MODEL_NAME, ALGORITHM_FULL_NAME);
  }

  @Test
  public void shouldSetDefaultValuesForOtherProperties() {
    // when
    ModelMetadata modelMetadata = modelMapper.apply(h2oModel);
    // then
    assertThat(modelMetadata.getFormat()).isEqualTo(DEFAULT_FORMAT);
    assertThat(modelMetadata.getStatus()).isEqualTo(DEFAULT_STATUS);
    assertThat(modelMetadata.getOwnerId()).isEqualTo(DEFAULT_OWNER_ID);
  }

  private H2oModel createExamplaryH2oModel() {
    H2oModel model = new H2oModel();
    H2oModelId modelId = new H2oModelId();
    modelId.setName(MODEL_NAME);
    model.setModelId(modelId);
    model.setAlgorithmAbbreviation(ALGORITHM_NAME);
    model.setAlgorithmFullName(ALGORITHM_FULL_NAME);
    return model;
  }

}