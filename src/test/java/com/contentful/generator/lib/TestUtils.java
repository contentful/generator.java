package com.contentful.generator.lib;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class TestUtils {
  private TestUtils() {
    throw new AssertionError();
  }

  public static String readTestResource(String fileName) throws IOException {
    return FileUtils.readFileToString(new File("src/test/resources/" + fileName), "UTF-8");
  }
}
