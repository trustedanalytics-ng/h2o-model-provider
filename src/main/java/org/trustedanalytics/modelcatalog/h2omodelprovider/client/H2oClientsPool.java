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

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;

import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstanceCredentials;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class H2oClientsPool {

  private final Supplier<Feign.Builder> clientSupplier;

  private final Map<String, H2oClient> clients;

  public H2oClientsPool(Supplier<Feign.Builder> clientSupplier) {
    this.clients = new HashMap<>();
    this.clientSupplier = clientSupplier;
  }

  public H2oClient takeOutClient(H2oInstanceCredentials h2oInstanceCredentials) {
    return clients.computeIfAbsent(
            h2oInstanceCredentials.getGuid(),
            guid -> prepareH2oClient(h2oInstanceCredentials));
  }

  private H2oClient prepareH2oClient(H2oInstanceCredentials h2oInstanceCredentials) {
    return new H2oClient(
            clientSupplier.get()
                    .requestInterceptor(prepareInterceptor(h2oInstanceCredentials))
                    .target(H2oOperations.class, "http://" + h2oInstanceCredentials.getHostname()),
            h2oInstanceCredentials);
  }

  private BasicAuthRequestInterceptor prepareInterceptor(H2oInstanceCredentials h2oInstanceCredentials) {
    return new BasicAuthRequestInterceptor(
            h2oInstanceCredentials.getLogin(), h2oInstanceCredentials.getPassword());
  }
}
