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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModel;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.H2oModelId;
import org.trustedanalytics.modelcatalog.h2omodelprovider.data.InstanceCredentials;

@RunWith(MockitoJUnitRunner.class)
public class RedisOperationsTest {

  @Mock RedissonClient redissonClient;

  @Mock RMap<Object, Object> modelsMap;

  @Test
  public void rememberModel_shallInsertToMap() {
    //arrange
    when(redissonClient.getMap(any())).thenReturn(modelsMap);
    RedisOperations sut = new RedisOperations(redissonClient, "models");
    H2oModel model = instantiateModel();

    //act
    sut.rememberModel(model);

    //assert
    verify(modelsMap).putIfAbsent(model.computeHash(), model);
  }

  @Test
  public void rememberModel_shallCatchEventualRedisExceptions() {
    //arrange
    when(modelsMap.putIfAbsent(any(), any())).thenThrow(new RedisException());
    when(redissonClient.getMap(any())).thenReturn(modelsMap);
    RedisOperations sut = new RedisOperations(redissonClient, "models");

    //act
    try {
      sut.rememberModel(instantiateModel());
    } catch (Exception e) {
      //assert
      fail("Should not throw any exception, but following was thrown:" + e.getMessage());
    }
  }

  @Test
  public void checkIfExists_shallReturnTrueWhenRedisContainsModel() {
    //arrange
    H2oModel model = instantiateModel();
    when(modelsMap.get(model.computeHash())).thenReturn(model);
    when(redissonClient.getMap(any())).thenReturn(modelsMap);
    RedisOperations sut = new RedisOperations(redissonClient, "models");

    //act
    boolean result = sut.checkIfExists(model);

    //assert
    assertTrue(result);
    verify(modelsMap).get(model.computeHash());
  }

  static H2oModel instantiateModel() {
    H2oModel model = new H2oModel();
    InstanceCredentials creds = new InstanceCredentials();
    creds.setId("123");
    model.setParentServerCredentials(creds);
    model.setTimestamp("123");
    H2oModelId modelId = new H2oModelId();
    model.setModelId(modelId);
    return model;
  }
}
