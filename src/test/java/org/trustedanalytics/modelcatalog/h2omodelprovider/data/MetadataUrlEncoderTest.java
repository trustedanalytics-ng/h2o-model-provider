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

import java.util.Collection;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class MetadataUrlEncoderTest {
  @Test
  public void shouldEncodeModelMetadataWithURLEncoding() {
    //act
    String result = MetadataUrlEncoder.encode(getTestModel());

    //assert
    Assert.assertTrue(result.contains("hostname=localhost%3A1234"));
    Assert.assertTrue(result.contains("login=johndoe"));
    Assert.assertTrue(result.contains("password=secret"));
    Assert.assertEquals(3, result.split("&").length);
  }

  public H2oModel getTestModel() {
    H2oModel model = new H2oModel();
    InstanceCredentials credentials = new InstanceCredentials();

    Collection<Metadata> metadata =
        Lists.newArrayList(
            new Metadata("login", "johndoe"),
            new Metadata("password", "secret"),
            new Metadata("hostname", "localhost:1234"));
    credentials.setMetadata(metadata);

    model.setParentServerCredentials(credentials);
    return model;
  }
}
