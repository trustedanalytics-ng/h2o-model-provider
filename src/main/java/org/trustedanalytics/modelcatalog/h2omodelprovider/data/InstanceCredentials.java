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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonIgnoreProperties({"bindings", "auditTrail"})
public class InstanceCredentials {
  @Setter
  private String id;
  @Setter
  private String name;
  @Setter
  private String type;
  @Setter
  private String classId;
  private Collection<Metadata> metadata;
  private Map<String, String> metadataMap;
  @Setter
  private String state;

  public void setMetadata(Collection<Metadata> metadata) {
    this.metadata = metadata;

    if(this.metadata != null)
        this.metadataMap = this.metadata.stream()
            .collect(Collectors.toMap(Metadata::getKey, Metadata::getValue));
    else
      this.metadataMap = null;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof InstanceCredentials)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    InstanceCredentials other = (InstanceCredentials) obj;
    return id.equals(other.getId());
  }
}
