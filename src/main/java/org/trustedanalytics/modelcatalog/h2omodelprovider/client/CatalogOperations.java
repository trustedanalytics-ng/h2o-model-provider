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

import org.trustedanalytics.modelcatalog.h2omodelprovider.data.Instance;

import java.util.Collection;
import feign.Headers;
import feign.Param;
import feign.RequestLine;


public interface CatalogOperations {
  @RequestLine("GET /api/v1/services")
  @Headers({"Authorization: {basicAuthBase64}", "Content-Type: application/json"})
  Collection<Instance> fetchOfferings(@Param("basicAuthBase64") String basicAuthBase64);

  @RequestLine("GET /api/v1/services/{serviceId}/instances")
  @Headers({"Authorization: {basicAuthBase64}", "Content-Type: application/json"})
  Collection<Instance> fetchAllCredentials(@Param("basicAuthBase64") String basicAuthBase64,
                                                         @Param("serviceId") String serviceId);
}
