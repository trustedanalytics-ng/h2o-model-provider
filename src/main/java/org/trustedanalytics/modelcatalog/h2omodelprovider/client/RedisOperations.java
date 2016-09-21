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

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;

public class RedisOperations implements DatabaseOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisOperations.class);

  RMap<String, H2oModel> modelsMap;

  public RedisOperations(RedissonClient database, String collectionName) {
    modelsMap = database.getMap(collectionName);
  }

  @Override
  public void rememberModel(H2oModel model) {
    try {
      modelsMap.putIfAbsent(model.computeHash(), model);
    } catch (RedisException e) {
      LOGGER.error("Could not insert model into internal database", e);
    }
  }

  @Override
  public boolean checkIfExists(H2oModel model) {
    try {
      LOGGER.debug("Looking for " + model.computeHash() + " in database");
      H2oModel existing = modelsMap.get(model.computeHash());
      return existing != null;
    } catch (RedisException e) {
      LOGGER.error("Could not check whether model exists in internal database", e);
      return false;
    }
  }
}
