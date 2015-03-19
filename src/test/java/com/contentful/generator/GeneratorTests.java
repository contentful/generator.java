package com.contentful.generator;

import com.contentful.generator.lib.TestUtils;
import com.contentful.java.cma.model.CMAContentType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Map;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class GeneratorTests extends BaseTest {
  @Test public void testBaseFields() throws Exception {
    generateAndAssert("base_fields.json", "base_fields_java.txt");
  }

  @Test public void testLinkToAsset() throws Exception {
    generateAndAssert("link_to_asset.json", "link_to_asset_java.txt");
  }

  @Test public void testLinkToEntry() throws Exception {
    generateAndAssert("link_to_entry.json", "link_to_entry_java.txt");
  }

  @Test public void testLinkToEntryTyped() throws Exception {
    generateAndAssert("link_to_entry_typed.json", "link_to_entry_typed_java.txt");
  }

  @Test public void testArrayOfAssets() throws Exception {
    generateAndAssert("array_of_assets.json", "array_of_assets_java.txt");
  }

  @Test public void testArrayOfEntries() throws Exception {
    generateAndAssert("array_of_entries.json", "array_of_entries_java.txt");
  }

  @Test public void testArrayOfEntriesTyped() throws Exception {
    generateAndAssert("array_of_entries_typed.json", "array_of_entries_typed_java.txt");
  }

  @Test public void testArrayOfSymbols() throws Exception {
    generateAndAssert("array_of_symbols.json", "array_of_symbols_java.txt");
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
      new Generator().createLinkFieldSpec("invalid", null, "test", "test");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Failed to create FieldSpec for \"test\"");
      throw e;
    }
  }

  void generateAndAssert(String responseFileName, String expectedCodeFileName) throws Exception {
    server.enqueue(newSuccessResponse(responseFileName));
    CMAContentType contentType = client.contentTypes().fetchOne("sid", "ctid");

    Generator generator = new Generator();
    generator.models.put("linked-id", "LinkedResource");

    String generatedSource = generator.generateModel("test", contentType, "Test").toString();
    assertThat(generatedSource).isEqualTo(TestUtils.readTestResource(expectedCodeFileName));
  }
}
