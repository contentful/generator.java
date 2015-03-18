package com.contentful.generator;

import com.contentful.java.cma.Constants.CMAFieldType;
import com.contentful.java.cma.model.CMAContentType;
import com.contentful.java.cma.model.CMAField;
import com.google.common.base.Joiner;
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

    assertThat(expectedSource).isEqualTo(generatedSource);
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
        "public class Lettuce {",
        "  private CDAAsset linkToAsset;",
        "",
        "  public Lettuce() {",
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
        "test", contentType, "Lettuce")
        .toString();

    assertThat(expectedSource).isEqualTo(generatedSource);
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
}
