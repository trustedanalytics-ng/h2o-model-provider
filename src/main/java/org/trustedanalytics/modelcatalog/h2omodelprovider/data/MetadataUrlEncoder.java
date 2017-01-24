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

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class MetadataUrlEncoder {
  
  private MetadataUrlEncoder() {  }

  public static String encode(H2oModel model) {
    Collection<BasicNameValuePair> metadataNameValuePairs = new HashSet<>();

    model.getParentServerCredentials().getMetadataMap().entrySet()
        .forEach(entry -> metadataNameValuePairs
            .add(new BasicNameValuePair(entry.getKey(), entry.getValue())));

    return URLEncodedUtils.format(metadataNameValuePairs, Charset.forName("UTF-8"));
  }
}
