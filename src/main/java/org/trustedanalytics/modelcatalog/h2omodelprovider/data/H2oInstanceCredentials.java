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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties({"bindings", "auditTrail"})
public class H2oInstanceCredentials {
  private String id;
  private String name;
  private String type;
  private String classId;
  private Collection<Metadata> metadata;
  private String state;
  //Both bindings and audits fields are out of scope of this component so they are skipped.

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof H2oInstanceCredentials)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    H2oInstanceCredentials other = (H2oInstanceCredentials) obj;
    return id.equals(other.getId());
  }
}
