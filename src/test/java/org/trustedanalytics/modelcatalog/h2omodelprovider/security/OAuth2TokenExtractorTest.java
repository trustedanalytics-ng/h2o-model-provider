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
package org.trustedanalytics.modelcatalog.h2omodelprovider.security;

import org.junit.Test;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuth2TokenExtractorTest {

  @Test
  public void shouldExtractTokenValueFromAuthenticationDetails() {
    // given
    OAuth2TokenExtractor oAuth2TokenExtractor = new OAuth2TokenExtractor();
    OAuth2Authentication authentication = mock(OAuth2Authentication.class);
    OAuth2AuthenticationDetails authenticationDetails = mock(OAuth2AuthenticationDetails.class);
    when(authentication.getDetails()).thenReturn(authenticationDetails);
    final String expectedToken = "xyz";
    when(authenticationDetails.getTokenValue()).thenReturn(expectedToken);
    // when
    String token = oAuth2TokenExtractor.apply(authentication);
    // then
    assertThat(token).isEqualTo(expectedToken);
  }
}