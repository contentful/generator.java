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

package com.contentful.generator;

import com.contentful.generator.lib.TestUtils;
import com.contentful.java.cma.Constants;
import com.contentful.java.cma.Constants.CMAFieldType;
import com.contentful.java.cma.model.CMAContentType;
import com.contentful.java.cma.model.CMAField;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

public class GeneratorTests extends BaseTest {
  @Test public void testBaseFields() throws Exception {
    generateAndAssert("base_fields.json", "BaseFields");
  }

  @Test public void testLinkToAsset() throws Exception {
    generateAndAssert("link_to_asset.json", "LinkToAsset");
  }

  @Test public void testLinkToEntry() throws Exception {
    generateAndAssert("link_to_entry.json", "LinkToEntry");
  }

  @Test public void testArrayOfAssets() throws Exception {
    generateAndAssert("array_of_assets.json", "ArrayOfAssets");
  }

  @Test public void testArrayOfEntriesTyped() throws Exception {
    generateAndAssert("array_of_entries.json", "ArrayOfEntries");
  }

  @Test public void testArrayOfSymbols() throws Exception {
    generateAndAssert("array_of_symbols.json", "ArrayOfSymbols");
  }

  @Test(expected = GeneratorException.class)
  public void testEntryWithNoTypeThrows() throws Exception {
    try {
      CMAField field =
          new CMAField().setId("fid").setType(CMAFieldType.Link).setLinkType("Entry");

      new Generator().createFieldSpec(field, "test", "name", "ctid");
    } catch (GeneratorException e) {
      assertEquals("Field \"fid\" for content type \"ctid\" is missing link validation, "
          + "must have content type validation.", e.getMessage());
      throw e;
    }
  }

  @Test(expected = GeneratorException.class)
  public void testArrayWithNoTypeThrows() throws Exception {
    try {
      CMAField field = new CMAField().setId("fid").setType(CMAFieldType.Array);

      field.setArrayItems(new HashMap(){{
        put("type", "Link");
        put("linkType", "Entry");
      }});

      new Generator().createFieldSpec(field, "test", "name", "ctid");
    } catch (GeneratorException e) {
      assertEquals("Field \"fid\" for content type \"ctid\" is missing link validation, "
              + "must have content type validation.", e.getMessage());
      throw e;
    }
  }

  @Test public void testExtractContentType() throws Exception {
    ArrayList<Map> validations = Lists.newArrayList();
    ArrayList<String> contentTypes = Lists.newArrayList("a");
    validations.add(ImmutableMap.of("linkContentType", contentTypes));
    String result = Generator.extractSingleLinkContentType(validations);
    assertThat(result).isEqualTo("a");
  }

  @Test public void testExtractContentTypeIsNullForMultipleTypes() throws Exception {
    ArrayList<Map> validations = Lists.newArrayList();
    ArrayList<String> contentTypes = Lists.newArrayList("a", "b");
    validations.add(ImmutableMap.of("linkContentType", contentTypes));
    String result = Generator.extractSingleLinkContentType(validations);
    assertThat(result).isNull();
  }

  @Test public void testExtractContentTypeIsNullForMultipleValidations()
      throws Exception {
    ArrayList<Map> validations = Lists.newArrayList();
    ArrayList<String> contentTypes = Lists.newArrayList("a");
    validations.add(ImmutableMap.of("linkContentType", contentTypes));
    validations.add(ImmutableMap.of("linkContentType", contentTypes));
    String result = Generator.extractSingleLinkContentType(validations);
    assertThat(result).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateLinkFieldSpecWithInvalidTypeThrows() throws Exception {
    try {
      new Generator().createLinkFieldSpec("invalid", null, "test", "test", "fieldId", "ctid");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Failed to create FieldSpec for \"test\"");
      throw e;
    }
  }

  @Test public void testGenerate() throws Exception {
    server.enqueue(newSuccessResponse("all_content_types.json"));
    Generator.FileHandler fileHandler = Mockito.mock(Generator.FileHandler.class);
    new Generator(fileHandler, null).generate("spaceid", "test", ".", client);

    Mockito.verify(fileHandler, Mockito.times(1)).write(Mockito.any(JavaFile.class), Mockito.anyString());
    Mockito.verify(fileHandler, Mockito.times(0)).delete(Mockito.any(File.class));
  }

  @Test public void testGenerateIgnoresContentTypeWithoutName() throws Exception {
    server.enqueue(newSuccessResponse("all_content_types_no_name.json"));
    Generator.FileHandler fileHandler = Mockito.mock(Generator.FileHandler.class);
    Generator.Printer printer = Mockito.mock(Generator.Printer.class);
    new Generator(fileHandler, printer).generate("spaceid", "test", ".", client);

    Mockito.verify(fileHandler, Mockito.times(1)).write(Mockito.any(JavaFile.class), Mockito.anyString());
    Mockito.verify(fileHandler, Mockito.times(0)).delete(Mockito.any(File.class));
    Mockito.verify(printer).print("WARNING: Ignoring Content Type (id=\"ctid\"), has no name.");
  }

  @Test public void testGenerateWrapsNetworkError() throws Exception {
    Generator.Printer printer = Mockito.mock(Generator.Printer.class);
    new Generator(null, printer).generate("spaceid", "test", ".", "invalid-access-token");
    Mockito.verify(printer).print("Failed to fetch content types, reason: 401 Unauthorized");
  }

  @Test(expected = GeneratorException.class)
  public void testGenerateCleansUpOnFailure() throws Exception {
    server.enqueue(newSuccessResponse("all_content_types_invalid_id.json"));
    Generator.FileHandler fileHandler = Mockito.mock(Generator.FileHandler.class);

    try {
      new Generator(fileHandler, null).generate("spaceid", "test", ".", client);
    } catch (GeneratorException e) {
      assertThat(e.getMessage()).isEqualTo(
          "java.lang.IllegalArgumentException: not a valid name: boolean");

      Mockito.verify(fileHandler, Mockito.times(1)).write(
          Mockito.any(JavaFile.class), Mockito.anyString());

      Mockito.verify(fileHandler, Mockito.times(1)).delete(Mockito.any(File.class));

      throw e;
    }
  }

  void generateAndAssert(String responseFileName, String className) throws Exception {
    server.enqueue(newSuccessResponse(responseFileName));
    CMAContentType contentType = client.contentTypes().fetchOne("sid", "ctid");

    Generator generator = new Generator();
    generator.models.put("linked-id", "LinkedResource");

    String generatedSource = generator.generateModel("test", contentType, className).toString();
    assertThat(generatedSource).isEqualTo(TestUtils.readTestResource(className + ".java"));
  }
}
