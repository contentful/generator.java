package com.contentful.generator;

import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.Constants;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAAsset;
import com.contentful.java.cma.model.CMAContentType;
import com.contentful.java.cma.model.CMAEntry;
import com.contentful.java.cma.model.CMAField;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import retrofit.RetrofitError;

public class Main {
  private static final Map<String, String> models = new HashMap<String, String>();

  public static void main(String[] args) {
    CommandLineParser parser = new GnuParser();
    Options options = constructOptions();
    try {
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("space")
          && line.hasOption("package")
          && line.hasOption("folder")
          && line.hasOption("token")) {

        generate(
            line.getOptionValue("space"),
            line.getOptionValue("token"),
            line.getOptionValue("package"),
            line.getOptionValue("folder"));
      } else {
        usage(args[0], options);
      }
    } catch (ParseException e) {
      System.err.println("Parsing failed, reason: " + e.getMessage());
    }
  }

  private static void generate(String spaceId, String token, String pkg, String path) {
    CMAClient client = new CMAClient.Builder()
        .setAccessToken(token)
        .build();

    try {
      CMAArray<CMAContentType> contentTypes = client.contentTypes().fetchAll(spaceId);

      for (CMAContentType contentType : contentTypes.getItems()) {
        String className = normalize(contentType.getName(), CaseFormat.UPPER_CAMEL);
        models.put(contentType.getResourceId(), className);
      }

      for (CMAContentType contentType : contentTypes.getItems()) {
        String name = contentType.getName();
        if (name == null || name.isEmpty()) {
          System.out.println("WARNING: Ignoring Content Type (id="
              + contentType.getResourceId()
              + "), has no name.");
          continue;
        }

        generateModel(pkg, contentType);
      }
    } catch (RetrofitError e) {
      System.out.println("Failed to fetch content types, reason: " + e.getMessage());
    }
  }

  private static void generateModel(String pkg, CMAContentType contentType) {
    String className = models.get(contentType.getResourceId());

    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

    for (CMAField field : contentType.getFields()) {
      String fieldName = normalize(field.getId(), CaseFormat.LOWER_CAMEL);
      FieldSpec fieldSpec = createFieldSpec(field, pkg, fieldName);

      builder.addField(fieldSpec)
          .addMethod(fieldGetter(fieldSpec))
          .addMethod(fieldSetter(fieldSpec));
    }

    JavaFile javaFile = JavaFile.builder(pkg, builder.build())
        .skipJavaLangImports(true)
        .build();

    try {
      javaFile.writeTo(System.out);
    } catch (IOException e) {
      System.out.println("Failed to write model for "
          + "\"" + contentType.getName() + "\", reason: " + e.getMessage());
      // TODO cleanup any generated files
    }
  }

  private static FieldSpec createFieldSpec(CMAField field, String pkg, String fieldName) {
    Constants.CMAFieldType fieldType = field.getType();
    switch (fieldType) {
      case Array:
        return createArrayFieldSpec(field, pkg, fieldName);
      case Link:
        return createLinkFieldSpec(field.getLinkType(), field.getValidations(), pkg, fieldName);
    }
    return fieldSpecForClass(fieldName, classForFieldType(fieldType));
  }

  private static FieldSpec fieldSpecForClass(String fieldName, Class clazz) {
    return FieldSpec.builder(clazz, fieldName, Modifier.PRIVATE).build();
  }

  private static FieldSpec createArrayFieldSpec(CMAField field, String pkg, String fieldName) {
    HashMap arrayItems = field.getArrayItems();
    if ("Link".equals(arrayItems.get("type"))) {
      String linkType = (String) arrayItems.get("linkType");
      if ("Asset".equals(linkType)) {
        return FieldSpec.builder(
            parameterizedList(CMAAsset.class),
            fieldName,
            Modifier.PRIVATE).build();
      } else if ("Entry".equals(linkType)) {
        //noinspection unchecked
        String linkContentType = extractSingleLinkContentType(
            (List<Map>) arrayItems.get("validations"));

        if (linkContentType == null) {
          return FieldSpec.builder(
              parameterizedList(CMAEntry.class),
              fieldName,
              Modifier.PRIVATE).build();
        } else {
          return FieldSpec.builder(
              parameterizedList(pkg, models.get(linkContentType)),
              fieldName,
              Modifier.PRIVATE).build();
        }
      }
    }
    return FieldSpec.builder(List.class, fieldName, Modifier.PRIVATE).build();
  }

  private static ParameterizedTypeName parameterizedList(Class clazz) {
    return ParameterizedTypeName.get(List.class, clazz);
  }

  private static ParameterizedTypeName parameterizedList(String pkg, String className) {
    return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(pkg, className));
  }

  private static FieldSpec createLinkFieldSpec(String linkType, List<Map> validations, String pkg,
      String fieldName) {
    Class clazz = null;
    ClassName className = null;

    if ("Asset".equals(linkType)) {
      clazz = CMAAsset.class;
    } else if ("Entry".equals(linkType)) {
      String linkContentType = extractSingleLinkContentType(validations);
      if (linkContentType == null) {
        clazz = CMAEntry.class;
      } else {
        className = ClassName.get(pkg, models.get(linkContentType));
      }
    }

    if (clazz != null) {
      return FieldSpec.builder(clazz, fieldName, Modifier.PRIVATE).build();
    } else if (className != null) {
      return FieldSpec.builder(className, fieldName, Modifier.PRIVATE).build();
    }

    throw new IllegalArgumentException("Failed to create FieldSpec for "
        + "\"" + fieldName + "\"");
  }

  private static String extractSingleLinkContentType(List<Map> validations) {
    String result = null;
    if (validations != null) {
      for (Map v : validations) {
        List linkContentType = (List) v.get("linkContentType");
        int size = linkContentType == null ? 0 : v.size();
        if (size > 1) {
          return null;
        } else if (size == 1) {
          if (result != null) {
            return null;
          }
          result = (String) linkContentType.get(0);
        }
      }
    }
    return result;
  }

  private static Class classForFieldType(Constants.CMAFieldType fieldType) {
    switch (fieldType) {
      case Boolean:
        return Boolean.class;
      case Date:
        return String.class;
      case Integer:
        return Integer.class;
      case Location:
        return Map.class;
      case Number:
        return Double.class;
      case Object:
        return Map.class;
      case Symbol:
        return String.class;
      case Text:
        return String.class;
    }

    throw new IllegalArgumentException("Unexpected field type: " + fieldType);
  }

  private static MethodSpec fieldSetter(FieldSpec fieldSpec) {
    String methodName = "set" + normalize(fieldSpec.name, CaseFormat.UPPER_CAMEL);
    return MethodSpec.methodBuilder(methodName)
        .addParameter(fieldSpec.type, fieldSpec.name)
        .returns(void.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("this.$N = $N", fieldSpec.name, fieldSpec.name)
        .build();
  }

  private static MethodSpec fieldGetter(FieldSpec fieldSpec) {
    String methodName = "get" + normalize(fieldSpec.name, CaseFormat.UPPER_CAMEL);
    return MethodSpec.methodBuilder(methodName)
        .returns(fieldSpec.type)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $N", fieldSpec)
        .build();
  }

  private static String normalize(String name, CaseFormat format) {
    String str = name.replaceAll("[^\\w\\d]", "_")
        .replaceAll("(.+)([A-Z]+)", "$1_$2")
        .toUpperCase();

    return CaseFormat.UPPER_UNDERSCORE.to(format, str);
  }

  public static Options constructOptions() {
    return new Options()
        .addOption("s", "space", true, "Space ID")
        .addOption("t", "token", true, "Management API Access Token")
        .addOption("p", "package", true, "Destination package name")
        .addOption("f", "folder", true, "Destination folder path");
  }

  public static void usage(String cmd, Options options) {
    new HelpFormatter().printHelp(cmd, options);
  }
}
