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
import org.trustedanalytics.modelcatalog.h2omodelprovider.exceptions.IncompleteMetadataException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;

public class H2oClientsPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oClientsPool.class);

  private final Supplier<Feign.Builder> clientSupplier;

  private final Map<String, H2oClient> clients;
  private static final String HOSTNAME_KEY = "hostname";
  private static final String LOGIN_KEY = "login";
  private static final String PASSWORD_KEY = "password";

  public H2oClientsPool(Supplier<Feign.Builder> clientSupplier) {
    this.clients = new HashMap<>();
    this.clientSupplier = clientSupplier;
  }

  public H2oClient takeOutClient(H2oInstanceCredentials h2oInstanceCredentials) {
    return clients.computeIfAbsent(
            h2oInstanceCredentials.getId(),
        guid -> {
          try {
            return prepareH2oClient(h2oInstanceCredentials);
          } catch (IncompleteMetadataException e) {
            LOGGER.error("Can't instantiate H2oClient out of its credentials", e);
            return null; //no mapping will be recorded (according to docs)
          }
        });
  }

  private H2oClient prepareH2oClient(H2oInstanceCredentials h2oInstanceCredentials)
      throws IncompleteMetadataException {
    Optional<Metadata> host = h2oInstanceCredentials.getMetadata().stream()
        .filter(i -> i.getKey().equals(HOSTNAME_KEY)).findFirst();

    String hostname = host.map(Metadata::getValue)
        .orElseThrow(() -> new IncompleteMetadataException(HOSTNAME_KEY, h2oInstanceCredentials.getName()));

    return new H2oClient(
            clientSupplier.get()
                    .requestInterceptor(prepareInterceptor(h2oInstanceCredentials))
                    .target(H2oOperations.class, "http://" + hostname),
            h2oInstanceCredentials);
  }

  private BasicAuthRequestInterceptor prepareInterceptor(H2oInstanceCredentials h2oInstanceCredentials)
      throws IncompleteMetadataException {
    Optional<Metadata> user = h2oInstanceCredentials.getMetadata().stream()
        .filter(i -> i.getKey().equals(LOGIN_KEY)).findFirst();
    Optional<Metadata> pass = h2oInstanceCredentials.getMetadata().stream()
        .filter(i -> i.getKey().equals(PASSWORD_KEY)).findFirst();
    String username = user.map(Metadata::getValue)
        .orElseThrow(() -> new IncompleteMetadataException(LOGIN_KEY, h2oInstanceCredentials.getName()));
    String password = pass.map(Metadata::getValue)
        .orElseThrow(() -> new IncompleteMetadataException(PASSWORD_KEY, h2oInstanceCredentials.getName()));
    return new BasicAuthRequestInterceptor(username, password);
  }
}
