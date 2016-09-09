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
package org.trustedanalytics.modelcatalog.h2omodelprovider.data;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class H2oInstanceCredentialsTest {

  private final UUID uuid = UUID.randomUUID();
  private final H2oInstanceCredentials h2oInstanceCredentials1 = new H2oInstanceCredentials();
  private final H2oInstanceCredentials h2oInstanceCredentialsEqualTo1 = new H2oInstanceCredentials();
  private final H2oInstanceCredentials h2oInstanceCredentialsNotEqualTo1 = new H2oInstanceCredentials();

  @Before
  public void setUp() {

    h2oInstanceCredentials1.setId(uuid.toString());
    h2oInstanceCredentialsEqualTo1.setId(uuid.toString());
    h2oInstanceCredentialsNotEqualTo1.setId(UUID.randomUUID().toString());
  }

  @Test
  public void shouldReturnGuidAsHashCodeAndKeepToHashCodeContract() {
    assertThat(h2oInstanceCredentials1.hashCode()).isEqualTo(h2oInstanceCredentials1.hashCode());
    assertThat(h2oInstanceCredentials1.hashCode()).isEqualTo(h2oInstanceCredentialsEqualTo1.hashCode());
    assertThat(h2oInstanceCredentials1.hashCode()).isNotEqualTo(h2oInstanceCredentialsNotEqualTo1.hashCode());
  }

  @Test
  public void shouldTestEqualityBasedOnGuidAndKeepToEqualsContract() {
    assertThat(h2oInstanceCredentials1).isEqualTo(h2oInstanceCredentials1);
    assertThat(h2oInstanceCredentials1).isEqualTo(h2oInstanceCredentialsEqualTo1);
    assertThat(h2oInstanceCredentialsEqualTo1).isEqualTo(h2oInstanceCredentials1);
    assertThat(h2oInstanceCredentials1).isNotEqualTo(h2oInstanceCredentialsNotEqualTo1);
    assertThat(h2oInstanceCredentials1).isNotEqualTo(new H2oInstanceCredentialsTest());
  }
}