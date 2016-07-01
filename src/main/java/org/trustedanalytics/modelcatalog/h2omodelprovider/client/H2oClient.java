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

import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;

import java.util.Collection;

public class H2oClient {
  
  private final H2oOperations h2oOperations;
  private final H2oInstanceCredentials h2oInstanceCredentials;

  H2oClient(H2oOperations h2oOperations, H2oInstanceCredentials h2oInstanceCredentials) {
    this.h2oOperations = h2oOperations;
    this.h2oInstanceCredentials = h2oInstanceCredentials;
  }

  public H2oInstance fetchH2oInstance() {
    Collection<H2oModel> models = fetchModels();
    return new H2oInstance(h2oInstanceCredentials, models);
  }

  private Collection<H2oModel> fetchModels() {
    return h2oOperations.fetchModels().getModels();
  }
}
