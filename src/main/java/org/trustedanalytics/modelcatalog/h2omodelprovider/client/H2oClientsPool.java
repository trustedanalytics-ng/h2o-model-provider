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
package org.trustedanalytics.modelcatalog.h2omodelprovider.client;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.InstanceCredentials;
import org.trustedanalytics.modelcatalog.h2omodelprovider.exceptions.IncompleteMetadataException;

public class H2oClientsPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oClientsPool.class);

  private final Supplier<Feign.Builder> clientSupplier;

  private final Map<String, H2oClient> clients;

  @Value("${services.catalog.hostname_key}")
  private String hostnameKey;

  @Value("${services.catalog.login_key}")
  private String loginKey;

  @Value("${services.catalog.pass_key}")
  private String passwordKey;

  public H2oClientsPool(Supplier<Feign.Builder> clientSupplier) {
    this.clients = new HashMap<>();
    this.clientSupplier = clientSupplier;
  }

  public H2oClient takeOutClient(InstanceCredentials instanceCredentials) {
    return clients.computeIfAbsent(
        instanceCredentials.getId(),
        guid -> {
          try {
            return prepareH2oClient(instanceCredentials);
          } catch (IncompleteMetadataException e) {
            LOGGER.error("Can't instantiate H2oClient out of its credentials", e);
            return null; //no mapping will be recorded (according to docs)
          }
        });
  }

  private H2oClient prepareH2oClient(InstanceCredentials instanceCredentials)
      throws IncompleteMetadataException {
    String hostname =
        Optional.ofNullable(instanceCredentials.getMetadataMap().get(hostnameKey))
            .orElseThrow(
                () -> new IncompleteMetadataException(hostnameKey, instanceCredentials.getName()));

    return new H2oClient(
        clientSupplier
            .get()
            .requestInterceptor(prepareInterceptor(instanceCredentials))
            .target(H2oOperations.class, hostname),
        instanceCredentials);
  }

  private BasicAuthRequestInterceptor prepareInterceptor(InstanceCredentials instanceCredentials)
      throws IncompleteMetadataException {
    String username =
        Optional.ofNullable(instanceCredentials.getMetadataMap().get(loginKey))
            .orElseThrow(
                () -> new IncompleteMetadataException(loginKey, instanceCredentials.getName()));
    String password =
        Optional.ofNullable(instanceCredentials.getMetadataMap().get(passwordKey))
            .orElseThrow(
                () -> new IncompleteMetadataException(passwordKey, instanceCredentials.getName()));

    return new BasicAuthRequestInterceptor(username, password);
  }
}
