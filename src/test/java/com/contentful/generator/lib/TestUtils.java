/*
 * Copyright (C) 2015 Contentful GmbH
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
