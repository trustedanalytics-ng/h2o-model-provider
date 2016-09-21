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

import static org.trustedanalytics.modelcatalog.rest.entities.ModelModificationParametersDTO.builder;

import com.google.common.base.Strings;
import java.util.HashSet;
import java.util.function.Function;
import org.trustedanalytics.modelcatalog.rest.entities.ModelModificationParametersDTO;

public class ModelMapper implements Function<H2oModel, ModelModificationParametersDTO> {

  private static final String CREATION_TOOL = "h2o";
  private static final String UNKNOWN_REVISION = "unknown";

  @Override
  public ModelModificationParametersDTO apply(H2oModel h2oModel) {
    String modelName = h2oModel.getModelId().getName();
    String algorithm = extractAlgorithmName(h2oModel);

    return builder()
        .name(modelName)
        .revision(UNKNOWN_REVISION)
        .algorithm(algorithm)
        .description(createDescription(modelName, algorithm))
        .creationTool(CREATION_TOOL)
        .artifactsIds(new HashSet<>())
        .build();
  }

  private String createDescription(String modelName, String algorithm) {
    return "'" + modelName + "' generated using " + algorithm + " algorithm";
  }

  private String extractAlgorithmName(H2oModel h2oModel) {
    String algoFullName = h2oModel.getAlgorithmFullName();
    return Strings.isNullOrEmpty(algoFullName) ? h2oModel.getAlgorithmAbbreviation() : algoFullName;
  }
}
