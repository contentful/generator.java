package com.contentful.generator;

import com.contentful.java.cma.Constants.CMAFieldType;
import com.contentful.java.cma.model.CMAContentType;
import com.contentful.java.cma.model.CMAField;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class GeneratorTests {
  @Test public void testBaseFields() throws Exception {
    CMAContentType contentType = new CMAContentType()
        .addField(new CMAField().setId("i1").setType(CMAFieldType.Boolean))
        .addField(new CMAField().setId("i2").setType(CMAFieldType.Date))
        .addField(new CMAField().setId("i3").setType(CMAFieldType.Integer))
        .addField(new CMAField().setId("i4").setType(CMAFieldType.Location))
        .addField(new CMAField().setId("i5").setType(CMAFieldType.Symbol))
        .addField(new CMAField().setId("i6").setType(CMAFieldType.Text));

    String expectedSource = Joiner.on('\n').join(
        "package test;",
        "",
        "import java.util.Map;",
        "",
        "public class Potato {",
        "  private Boolean i1;",
        "",
        "  private String i2;",
        "",
        "  private Integer i3;",
        "",
        "  private Map i4;",
        "",
        "  private String i5;",
        "",
        "  private String i6;",
        "",
        "  public Potato() {",
        "  }",
        "",
        "  public Boolean getI1() {",
        "    return i1;",
        "  }",
        "",
        "  public void setI1(Boolean i1) {",
        "    this.i1 = i1;",
        "  }",
        "",
        "  public String getI2() {",
        "    return i2;",
        "  }",
        "",
        "  public void setI2(String i2) {",
        "    this.i2 = i2;",
        "  }",
        "",
        "  public Integer getI3() {",
        "    return i3;",
        "  }",
        "",
        "  public void setI3(Integer i3) {",
        "    this.i3 = i3;",
        "  }",
        "",
        "  public Map getI4() {",
        "    return i4;",
        "  }",
        "",
        "  public void setI4(Map i4) {",
        "    this.i4 = i4;",
        "  }",
        "",
        "  public String getI5() {",
        "    return i5;",
        "  }",
        "",
        "  public void setI5(String i5) {",
        "    this.i5 = i5;",
        "  }",
        "",
        "  public String getI6() {",
        "    return i6;",
        "  }",
        "",
        "  public void setI6(String i6) {",
        "    this.i6 = i6;",
        "  }",
        "}",
        "");

    String generatedSource = new Generator().generateModel(
        "test", contentType, "Potato").toString();

    assertThat(generatedSource).isEqualTo(expectedSource);
  }

  @Test public void testLinkAsset() throws Exception {
    CMAContentType contentType = new CMAContentType();

    CMAField field = new CMAField().setId("linkToAsset")
        .setType(CMAFieldType.Link)
        .setLinkType("Asset");

    contentType.addField(field);

    String expectedSource = Joiner.on('\n').join(
        "package test;",
        "",
        "import com.contentful.java.cda.model.CDAAsset;",
        "",
        "public class Onion {",
        "  private CDAAsset linkToAsset;",
        "",
        "  public Onion() {",
        "  }",
        "",
        "  public CDAAsset getLinkToAsset() {",
        "    return linkToAsset;",
        "  }",
        "",
        "  public void setLinkToAsset(CDAAsset linkToAsset) {",
        "    this.linkToAsset = linkToAsset;",
        "  }",
        "}",
        "");

    String generatedSource = new Generator().generateModel(
        "test", contentType, "Onion")
        .toString();

    assertThat(generatedSource).isEqualTo(expectedSource);
  }

  @Test public void testLinkEntry() throws Exception {
    CMAContentType contentType = new CMAContentType();

    CMAField field = new CMAField().setId("linkToEntry")
        .setType(CMAFieldType.Link)
        .setLinkType("Entry");

    contentType.addField(field);

    String expectedSource = Joiner.on('\n').join(
        "package test;",
        "",
        "import com.contentful.java.cda.model.CDAEntry;",
        "",
        "public class Carrot {",
        "  private CDAEntry linkToEntry;",
        "",
        "  public Carrot() {",
        "  }",
        "",
        "  public CDAEntry getLinkToEntry() {",
        "    return linkToEntry;",
        "  }",
        "",
        "  public void setLinkToEntry(CDAEntry linkToEntry) {",
        "    this.linkToEntry = linkToEntry;",
        "  }",
        "}",
        "");

    String generatedSource = new Generator().generateModel(
        "test", contentType, "Carrot")
        .toString();

    assertThat(expectedSource).isEqualTo(generatedSource);
  }

  @Test public void testLinkEntryOfType() throws Exception {
    CMAContentType contentType = new CMAContentType();

    CMAField field = new CMAField().setId("linkToEntry")
        .setType(CMAFieldType.Link)
        .setLinkType("Entry");

    field.setValidations(Lists.<Map>newArrayList(ImmutableMap.of(
        "linkContentType", Lists.newArrayList("abc"))));

    contentType.addField(field);

    String expectedSource = Joiner.on('\n').join(
        "package test;",
        "",
        "public class Zucchini {",
        "  private Pumpkin linkToEntry;",
        "",
        "  public Zucchini() {",
        "  }",
        "",
        "  public Pumpkin getLinkToEntry() {",
        "    return linkToEntry;",
        "  }",
        "",
        "  public void setLinkToEntry(Pumpkin linkToEntry) {",
        "    this.linkToEntry = linkToEntry;",
        "  }",
        "}",
        "");

    CMAContentType linkedContentType = new CMAContentType().setId("abc").setName("Pumpkin")
        .addField(new CMAField().setId("whatever").setType(CMAFieldType.Text));

    Generator generator = new Generator();
    generator.models.put(linkedContentType.getResourceId(), "Pumpkin");

    String generatedSource = generator.generateModel("test", contentType, "Zucchini")
        .toString();

    assertThat(generatedSource).isEqualTo(expectedSource);
  }

  @Test public void testAssetsArray() throws Exception {
    CMAContentType contentType = new CMAContentType();

    HashMap<String, String> map = Maps.newHashMap();
    map.put("type", CMAFieldType.Link.toString());
    map.put("linkType", "Asset");

    contentType.addField(new CMAField().setId("array")
        .setType(CMAFieldType.Array)
        .setArrayItems(map));

    String expectedSource = Joiner.on('\n').join(
        "package test;",
        "",
        "import com.contentful.java.cda.model.CDAAsset;",
        "import java.util.List;",
        "",
        "public class Celery {",
        "  private List<CDAAsset> array;",
        "",
        "  public Celery() {",
        "  }",
        "",
        "  public List<CDAAsset> getArray() {",
        "    return array;",
        "  }",
        "",
        "  public void setArray(List<CDAAsset> array) {",
        "    this.array = array;",
        "  }",
        "}",
        "");

    String generatedSource = new Generator().generateModel(
        "test", contentType, "Celery")
        .toString();

    assertThat(expectedSource).isEqualTo(generatedSource);
  }

  @Test public void testEntriesArray() throws Exception {
    CMAContentType contentType = new CMAContentType();

    HashMap<String, String> map = Maps.newHashMap();
    map.put("type", CMAFieldType.Link.toString());
    map.put("linkType", "Entry");

    contentType.addField(new CMAField().setId("array")
        .setType(CMAFieldType.Array)
        .setArrayItems(map));

    String expectedSource = Joiner.on('\n').join(
        "package test;",
        "",
        "import com.contentful.java.cda.model.CDAEntry;",
        "import java.util.List;",
        "",
        "public class Parsley {",
        "  private List<CDAEntry> array;",
        "",
        "  public Parsley() {",
        "  }",
        "",
        "  public List<CDAEntry> getArray() {",
        "    return array;",
        "  }",
        "",
        "  public void setArray(List<CDAEntry> array) {",
        "    this.array = array;",
        "  }",
        "}",
        "");

    String generatedSource = new Generator().generateModel(
        "test", contentType, "Parsley")
        .toString();

    assertThat(generatedSource).isEqualTo(expectedSource);
  }

  @Test @SuppressWarnings("unchecked")
  public void testEntriesArrayOfType() throws Exception {
    CMAContentType contentType = new CMAContentType();

    HashMap map = Maps.newHashMap();
    map.put("type", CMAFieldType.Link.toString());
    map.put("linkType", "Entry");
    map.put("validations", Lists.<Map>newArrayList(ImmutableMap.of(
        "linkContentType", Lists.newArrayList("abc"))));

    CMAField field = new CMAField().setId("array").setType(CMAFieldType.Array).setArrayItems(map);

    contentType.addField(field);

    String expectedSource = Joiner.on('\n').join(
        "package test;",
        "",
        "import java.util.List;",
        "",
        "public class Salt {",
        "  private List<Pepper> array;",
        "",
        "  public Salt() {",
        "  }",
        "",
        "  public List<Pepper> getArray() {",
        "    return array;",
        "  }",
        "",
        "  public void setArray(List<Pepper> array) {",
        "    this.array = array;",
        "  }",
        "}",
        "");

    CMAContentType linkedContentType = new CMAContentType().setId("abc").setName("Pepper")
        .addField(new CMAField().setId("whatever").setType(CMAFieldType.Text));

    Generator generator = new Generator();
    generator.models.put(linkedContentType.getResourceId(), "Pepper");

    String generatedSource = generator.generateModel("test", contentType, "Salt").toString();

    assertThat(generatedSource).isEqualTo(expectedSource);
  }
}
