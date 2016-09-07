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

import java.util.Collection;
import java.util.Collections;

import lombok.Getter;

@Getter
public class H2oInstance {

  private final H2oInstanceCredentials instanceCredentials;
  private final Collection<H2oModel> models;

  public H2oInstance(H2oInstanceCredentials instanceCredentials, Collection<H2oModel> models) {
    this.instanceCredentials = instanceCredentials;
    this.models = Collections.unmodifiableCollection(models);
  }
}
