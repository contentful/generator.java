package com.contentful.generator;

import com.contentful.generator.lib.TestUtils;
import com.contentful.java.cma.CMAClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import retrofit.RestAdapter;

public class BaseTest {
  MockWebServer server;
  CMAClient client;

  @Before public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();

    client = new CMAClient.Builder()
        .setAccessToken("supersecret")
        .setEndpoint(server.getUrl("/").toString())
        .setLogLevel(RestAdapter.LogLevel.FULL)
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
