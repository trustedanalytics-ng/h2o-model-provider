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
package org.trustedanalytics.modelcatalog.h2omodelprovider.service;

import org.trustedanalytics.modelcatalog.h2omodelprovider.client.H2oClientsPool;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstance;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstanceCredentials;

import com.google.common.cache.CacheLoader;

public class H2oInstanceCacheLoader extends CacheLoader<H2oInstanceCredentials, H2oInstance> {

  private H2oClientsPool h2oClientsPool;

  public H2oInstanceCacheLoader(H2oClientsPool h2oClientsPool) {
    this.h2oClientsPool = h2oClientsPool;
  }

  @Override
  public H2oInstance load(H2oInstanceCredentials h2oInstanceCredentials) {
    return h2oClientsPool.takeOutClient(h2oInstanceCredentials).fetchH2oInstance();
  }

}
