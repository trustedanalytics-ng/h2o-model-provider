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

import com.google.common.base.Strings;

import org.trustedanalytics.modelcatalog.rest.api.ModelMetadata;
import org.trustedanalytics.modelcatalog.rest.api.ModelStatus;

import java.util.function.Function;

class ModelMapper implements Function<H2oModel, ModelMetadata> {

  private static final String DEFAULT_FORMAT = "h2o";
  private static final ModelStatus DEFAULT_STATUS = ModelStatus.DRAFT;
  private static final String DEFAULT_OWNER_ID = "h2o user";

  private String h2oServerId;

  ModelMapper(String h2oServerId) {
    this.h2oServerId = h2oServerId;
  }

  @Override
  public ModelMetadata apply(H2oModel h2oModel) {
    ModelMetadata modelMetadata = new ModelMetadata();

    String modelName = h2oModel.getModelId().getName();
    modelMetadata.setId(createId(modelName));
    modelMetadata.setName(modelName);

    modelMetadata.setFormat(DEFAULT_FORMAT);

    modelMetadata.setAlgorithm(extractAlgorithmName(h2oModel));
    modelMetadata.setDescription(createDescription(modelName, modelMetadata.getAlgorithm()));

    modelMetadata.setStatus(DEFAULT_STATUS);
    modelMetadata.setOwnerId(DEFAULT_OWNER_ID);

    return modelMetadata;
  }

  private String createId(String modelName) {
    return h2oServerId + "_" + modelName;
  }

  private String createDescription(String modelName, String algorithm) {
    return "'" + modelName + "' generated using " + algorithm + " algorithm";
  }

  private String extractAlgorithmName(H2oModel h2oModel) {
    String algoFullName = h2oModel.getAlgorithmFullName();
    return Strings.isNullOrEmpty(algoFullName) ? h2oModel.getAlgorithmAbbreviation() : algoFullName;
  }

}
