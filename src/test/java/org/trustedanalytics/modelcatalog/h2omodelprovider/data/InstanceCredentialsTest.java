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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class InstanceCredentialsTest {

  private final UUID uuid = UUID.randomUUID();
  private final InstanceCredentials instanceCredentials1 = new InstanceCredentials();
  private final InstanceCredentials instanceCredentialsEqualTo1 = new InstanceCredentials();
  private final InstanceCredentials instanceCredentialsNotEqualTo1 = new InstanceCredentials();

  @Before
  public void setUp() {

    instanceCredentials1.setId(uuid.toString());
    instanceCredentialsEqualTo1.setId(uuid.toString());
    instanceCredentialsNotEqualTo1.setId(UUID.randomUUID().toString());
  }

  @Test
  public void shouldReturnGuidAsHashCodeAndKeepToHashCodeContract() {
    assertThat(instanceCredentials1.hashCode()).isEqualTo(instanceCredentials1.hashCode());
    assertThat(instanceCredentials1.hashCode()).isEqualTo(instanceCredentialsEqualTo1.hashCode());
    assertThat(instanceCredentials1.hashCode())
        .isNotEqualTo(instanceCredentialsNotEqualTo1.hashCode());
  }

  @Test
  public void shouldTestEqualityBasedOnGuidAndKeepToEqualsContract() {
    assertThat(instanceCredentials1).isEqualTo(instanceCredentials1);
    assertThat(instanceCredentials1).isEqualTo(instanceCredentialsEqualTo1);
    assertThat(instanceCredentialsEqualTo1).isEqualTo(instanceCredentials1);
    assertThat(instanceCredentials1).isNotEqualTo(instanceCredentialsNotEqualTo1);
    assertThat(instanceCredentials1).isNotEqualTo(new InstanceCredentialsTest());
  }
}
