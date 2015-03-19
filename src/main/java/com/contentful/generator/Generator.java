package com.contentful.generator;

import com.contentful.java.cda.model.CDAAsset;
import com.contentful.java.cda.model.CDAEntry;
import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.Constants;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAContentType;
import com.contentful.java.cma.model.CMAField;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import retrofit.RetrofitError;

public class Generator {
  final Map<String, String> models;

  public Generator() {
    models = new HashMap<String, String>();
  }

  /**
   * Fetch content types from the given space and generate corresponding POJOs.
   *
   * @param spaceId space id
   * @param pkg package name for generated classes
   * @param path package source root
   * @param client management api client instance
   */
  public void generate(String spaceId, String pkg, String path, CMAClient client) {
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

        JavaFile javaFile = generateModel(pkg, contentType, models.get(contentType.getResourceId()));
        javaFile.writeTo(new File(path));
      }
    } catch (RetrofitError e) {
      System.out.println("Failed to fetch content types, reason: " + e.getMessage());
    } catch (Exception e) {
      // Clean up any generated files
      String generatedPath = Joiner.on(File.separatorChar).join(
          path,
          Joiner.on(File.separatorChar).join(pkg.split("\\.")));

      for (String fileName : models.values()) {
        //noinspection ResultOfMethodCallIgnored
        new File(generatedPath + File.separator + fileName + ".java").delete();
      }

      throw new RuntimeException(e);
    }
  }

  /**
   * Convenience method around {@link #generate} that creates a {@link CMAClient}
   * per the given {@code token}.
   *
   * @param spaceId space id
   * @param pkg package name for generated classes
   * @param path package source root
   * @param token management api access token
   */
  public void generate(String spaceId, String pkg, String path, String token) {
    CMAClient client = new CMAClient.Builder()
        .setAccessToken(token)
        .build();

    generate(spaceId, pkg, path, client);
  }

  JavaFile generateModel(String pkg, CMAContentType contentType, String className)
      throws Exception {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

    for (CMAField field : contentType.getFields()) {
      if (field.isDisabled()) {
        continue;
      }

      String fieldName = normalize(field.getId(), CaseFormat.LOWER_CAMEL);
      FieldSpec fieldSpec = createFieldSpec(field, pkg, fieldName);

      builder.addField(fieldSpec)
          .addMethod(fieldGetter(fieldSpec))
          .addMethod(fieldSetter(fieldSpec));
    }

    return JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
  }

  FieldSpec createArrayFieldSpec(CMAField field, String pkg, String fieldName) {
    HashMap arrayItems = field.getArrayItems();
    if ("Link".equals(arrayItems.get("type"))) {
      String linkType = (String) arrayItems.get("linkType");
      if ("Asset".equals(linkType)) {
        return FieldSpec.builder(
            parameterizedList(CDAAsset.class),
            fieldName,
            Modifier.PRIVATE).build();
      } else if ("Entry".equals(linkType)) {
        //noinspection unchecked
        String linkContentType = extractSingleLinkContentType(
            (List<Map>) arrayItems.get("validations"));

        if (linkContentType == null) {
          return FieldSpec.builder(
              parameterizedList(CDAEntry.class),
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

  FieldSpec createFieldSpec(CMAField field, String pkg, String fieldName) {
    Constants.CMAFieldType fieldType = field.getType();
    switch (fieldType) {
      case Array:
        return createArrayFieldSpec(field, pkg, fieldName);
      case Link:
        return createLinkFieldSpec(field.getLinkType(), field.getValidations(), pkg, fieldName);
    }
    return fieldSpecForClass(fieldName, classForFieldType(fieldType));
  }

  FieldSpec createLinkFieldSpec(String linkType, List<Map> validations, String pkg,
      String fieldName) {
    Class clazz = null;
    ClassName className = null;

    if ("Asset".equals(linkType)) {
      clazz = CDAAsset.class;
    } else if ("Entry".equals(linkType)) {
      String linkContentType = extractSingleLinkContentType(validations);
      if (linkContentType == null) {
        clazz = CDAEntry.class;
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

  static FieldSpec fieldSpecForClass(String fieldName, Class clazz) {
    return FieldSpec.builder(clazz, fieldName, Modifier.PRIVATE).build();
  }

  static ParameterizedTypeName parameterizedList(Class clazz) {
    return ParameterizedTypeName.get(List.class, clazz);
  }

  static ParameterizedTypeName parameterizedList(String pkg, String className) {
    return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(pkg, className));
  }

  static String extractSingleLinkContentType(List<Map> validations) {
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

  static Class classForFieldType(Constants.CMAFieldType fieldType) {
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

  static MethodSpec fieldSetter(FieldSpec fieldSpec) {
    String methodName = "set" + normalize(fieldSpec.name, CaseFormat.UPPER_CAMEL);
    return MethodSpec.methodBuilder(methodName)
        .addParameter(fieldSpec.type, fieldSpec.name)
        .returns(void.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("this.$N = $N", fieldSpec.name, fieldSpec.name)
        .build();
  }

  static MethodSpec fieldGetter(FieldSpec fieldSpec) {
    String methodName = "get" + normalize(fieldSpec.name, CaseFormat.UPPER_CAMEL);
    return MethodSpec.methodBuilder(methodName)
        .returns(fieldSpec.type)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $N", fieldSpec)
        .build();
  }

  static String normalize(String name, CaseFormat format) {
    return CaseFormat.LOWER_CAMEL.to(format, name.replaceAll("[^\\w\\d]", ""));
  }
}
