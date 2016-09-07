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

import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oInstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.Metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;

public class H2oClientsPool {

  private final Supplier<Feign.Builder> clientSupplier;

  private final Map<String, H2oClient> clients;

  public H2oClientsPool(Supplier<Feign.Builder> clientSupplier) {
    this.clients = new HashMap<>();
    this.clientSupplier = clientSupplier;
  }

  public H2oClient takeOutClient(H2oInstanceCredentials h2oInstanceCredentials) {
    return clients.computeIfAbsent(
            h2oInstanceCredentials.getId(),
            guid -> prepareH2oClient(h2oInstanceCredentials));
  }

  private H2oClient prepareH2oClient(H2oInstanceCredentials h2oInstanceCredentials) {
    Optional<Metadata> host = h2oInstanceCredentials.getMetadata().stream()
        .filter(i -> i.getKey().equals("hostname")).findFirst();

    //TODO: host.get shall be checked with ifPresent before!
    return new H2oClient(
            clientSupplier.get()
                    .requestInterceptor(prepareInterceptor(h2oInstanceCredentials))
                    .target(H2oOperations.class, "http://" + host.get().getValue()),
            h2oInstanceCredentials);
  }

  private BasicAuthRequestInterceptor prepareInterceptor(H2oInstanceCredentials h2oInstanceCredentials) {
    //TODO: Cover all paths here!
    Optional<Metadata> user = h2oInstanceCredentials.getMetadata().stream()
        .filter(i -> i.getKey().equals("login")).findFirst();
    Optional<Metadata> pass = h2oInstanceCredentials.getMetadata().stream()
        .filter(i -> i.getKey().equals("password")).findFirst();
    return new BasicAuthRequestInterceptor(user.get().getValue(), pass.get().getValue());
  }
}
