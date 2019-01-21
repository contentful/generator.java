/*
 * Copyright (C) 2017 Contentful GmbH
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

package com.contentful.generator;

import com.contentful.generator.lib.TestUtils;
import com.contentful.java.cma.CMAClient;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class BaseTest {
  MockWebServer server;
  CMAClient client;

  @Before public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();

    client = new CMAClient.Builder()
        .setAccessToken("supersecret")
        .setCoreEndpoint(server.url("/").toString())
        .setSpaceId("nospace")
        .setEnvironmentId("noenv")
        .build();
  }

  @After public void tearDown() throws Exception {
    server.shutdown();
  }

  static MockResponse newSuccessResponse(String resourceFileName) throws IOException {
    return new MockResponse().setResponseCode(200).setBody(
        TestUtils.readTestResource(resourceFileName));
  }
}
