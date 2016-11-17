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
package org.trustedanalytics.modelcatalog.h2omodelprovider.data;

import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trustedanalytics.modelcatalog.h2omodelprovider.client.DatabaseOperations;

@Service
public class ModelFilter implements Predicate<H2oModel> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelFilter.class);

  private final DatabaseOperations database;

  @Autowired
  public ModelFilter(DatabaseOperations database) {
    this.database = database;
  }

  @Override
  public boolean test(H2oModel h2oModel) {
    boolean modelCanBeAdded = !database.checkIfExists(h2oModel) && h2oModel.isComplete();
    LOGGER.info("Pushing <{}> to model-catalog: {}", h2oModel.getModelId().getName(), modelCanBeAdded);
    return modelCanBeAdded;
  }
}
