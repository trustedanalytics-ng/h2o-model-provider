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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class H2oModel {

  @JsonProperty("timestamp")
  private long timestamp;

  @JsonProperty("algo_full_name")
  private String algorithmFullName;

  @JsonProperty("algo")
  private String algorithmAbbreviation;

  @JsonProperty("model_id")
  private H2oModelId modelId;

  @JsonIgnore private InstanceCredentials parentServerCredentials;

  public String computeHash() {
    String toHash = parentServerCredentials.getId() + modelId.getName() + timestamp;
    Charset charset = Charset.availableCharsets().get("UTF-8");
    return Hashing.sha1().hashString(toHash, charset).toString();
  }

  public boolean isComplete() {
    return timestamp > 0;
  }
}
