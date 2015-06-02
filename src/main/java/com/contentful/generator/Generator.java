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

import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.Constants;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAContentType;
import com.contentful.java.cma.model.CMAField;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import retrofit.RetrofitError;

public class Generator {
  final FileHandler fileHandler;
  final Printer printer;
  final Map<String, String> models;

  Generator(FileHandler fileHandler, Printer printer) {
    this.fileHandler = fileHandler == null ? new DefaultFileHandler() : fileHandler;
    this.printer = printer == null ? new DefaultPrinter() : printer;
    this.models = new HashMap<String, String>();
  }

  public Generator() {
    this(null, null);
  }

  /**
   * Fetch content types from the given space and generate corresponding model classes.
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
        String name = contentType.getName();
        if (name != null) {
          String className = normalize(name, CaseFormat.UPPER_CAMEL);
          models.put(contentType.getResourceId(), className);
        }
      }

      for (CMAContentType contentType : contentTypes.getItems()) {
        String name = contentType.getName();
        if (name == null || name.isEmpty()) {
          printer.print("WARNING: Ignoring Content Type (id="
              + "\"" + contentType.getResourceId() + "\""
              + "), has no name.");
          continue;
        }

        JavaFile javaFile = generateModel(pkg, contentType,
            models.get(contentType.getResourceId()));

        fileHandler.write(javaFile, path);
      }
    } catch (RetrofitError e) {
      printer.print("Failed to fetch content types, reason: " + e.getMessage());
    } catch (Exception e) {
      // Clean up any generated files
      String generatedPath = Joiner.on(File.separatorChar).join(
          path,
          Joiner.on(File.separatorChar).join(pkg.split("\\.")));

      for (String fileName : models.values()) {
        fileHandler.delete(new File(generatedPath + File.separator + fileName + ".java"));
      }

      throw new GeneratorException(e);
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

  AnnotationSpec annotateModel(CMAContentType contentType) {
    return AnnotationSpec.builder(ContentType.class)
        .addMember("value", "$S", contentType.getSys().get("id"))
        .build();
  }

  AnnotationSpec annotateField(String id, String fieldName) {
    AnnotationSpec.Builder builder = AnnotationSpec.builder(Field.class);
    if (!id.equals(fieldName)) {
      builder.addMember("value", "$S", id);
    }
    return builder.build();
  }

  JavaFile generateModel(String pkg, CMAContentType contentType, String className)
      throws Exception {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .superclass(ClassName.get("com.contentful.vault", "Resource"))
        .addAnnotation(annotateModel(contentType));

    for (CMAField field : contentType.getFields()) {
      if (field.isDisabled()) {
        continue;
      }

      String fieldName = normalize(field.getId(), CaseFormat.LOWER_CAMEL);
      FieldSpec fieldSpec = createFieldSpec(field, pkg, fieldName, contentType.getResourceId());

      builder.addField(fieldSpec)
          .addMethod(fieldGetter(fieldSpec));
    }

    return JavaFile.builder(pkg, builder.build()).skipJavaLangImports(true).build();
  }

  FieldSpec createArrayFieldSpec(CMAField field, String pkg, String fieldName,
      String parentContentTypeId) {
    Map arrayItems = field.getArrayItems();
    String fieldId = field.getId();
    if ("Link".equals(arrayItems.get("type"))) {
      String linkType = (String) arrayItems.get("linkType");
      if ("Asset".equals(linkType)) {
        return fieldBuilder(
            parameterizedList("com.contentful.vault", "Asset"), fieldName, fieldId).build();
      } else if ("Entry".equals(linkType)) {
        //noinspection unchecked
        String linkContentType = extractSingleLinkContentType(
            (List<Map>) arrayItems.get("validations"));

        if (linkContentType == null) {
          throwLinkNoContentType(parentContentTypeId, fieldId);
        } else {
          return fieldBuilder(parameterizedList(pkg, models.get(linkContentType)), fieldName,
              fieldId).build();
        }
      } else {
        throw new GeneratorException("Invalid array linkType.");
      }
    }
    return fieldBuilder(ClassName.get(List.class), fieldName, fieldId).build();
  }

  FieldSpec.Builder fieldBuilder(TypeName type, String fieldName, String fieldId) {
    FieldSpec.Builder builder = FieldSpec.builder(type, fieldName);
    AnnotationSpec.Builder annotation = AnnotationSpec.builder(Field.class);
    if (!fieldId.equals(fieldName)) {
      annotation.addMember("value", "$S", fieldId);
    }
    return builder.addAnnotation(annotation.build());
  }

  FieldSpec createFieldSpec(CMAField field, String pkg, String fieldName,
      String parentContentTypeId) {
    Constants.CMAFieldType fieldType = field.getType();
    String fieldId = field.getId();
    switch (fieldType) {
      case Array:
        return createArrayFieldSpec(field, pkg, fieldName, parentContentTypeId);
      case Link:
        return createLinkFieldSpec(field.getLinkType(), field.getValidations(), pkg, fieldName,
            fieldId, parentContentTypeId);
    }
    return fieldBuilder(ClassName.get(classForFieldType(fieldType)), fieldName, fieldId).build();
  }

  FieldSpec createLinkFieldSpec(String linkType, List<Map> validations, String pkg,
      String fieldName, String fieldId, String parentContentTypeId) {
    ClassName className = null;

    if ("Asset".equals(linkType)) {
      className = ClassName.get("com.contentful.vault", "Asset");
    } else if ("Entry".equals(linkType)) {
      String linkContentType = extractSingleLinkContentType(validations);
      if (linkContentType == null) {
        throwLinkNoContentType(parentContentTypeId, fieldId);
      } else {
        className = ClassName.get(pkg, models.get(linkContentType));
      }
    }

    if (className != null) {
      return fieldBuilder(className, fieldName, fieldId).build();
    }

    throw new IllegalArgumentException("Failed to create FieldSpec for "
        + "\"" + fieldName + "\"");
  }

  static void throwLinkNoContentType(String contentTypeId, String fieldId) {
    throw new GeneratorException(String.format(
        "Field \"%s\" for content type \"%s\" is missing link validation, "
            + "must have content type validation.",
        fieldId, contentTypeId));
  }

  static ParameterizedTypeName parameterizedList(String pkg, String className) {
    return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(pkg, className));
  }

  static String extractSingleLinkContentType(List<Map> validations) {
    String result = null;
    if (validations != null) {
      for (Map v : validations) {
        List linkContentType = (List) v.get("linkContentType");
        int size = linkContentType == null ? 0 : linkContentType.size();
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
      default:
        return String.class;
    }
  }

  static MethodSpec fieldGetter(FieldSpec fieldSpec) {
    String methodName = normalize(fieldSpec.name, CaseFormat.LOWER_CAMEL);
    return MethodSpec.methodBuilder(methodName)
        .returns(fieldSpec.type)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $N", fieldSpec)
        .build();
  }

  static String normalize(String name, CaseFormat format) {
    String normalized = name.substring(0, 1).toLowerCase();
    if (name.length() > 1) {
      normalized += name.substring(1);
    }
    return CaseFormat.LOWER_CAMEL.to(format, normalized.replaceAll("[^\\w\\d]", ""));
  }

  interface FileHandler {
    void write(JavaFile javaFile, String path) throws IOException;
    boolean delete(File file);
  }

  interface Printer {
    void print(String text);
  }

  static class DefaultFileHandler implements FileHandler {
    @Override public void write(JavaFile javaFile, String path) throws IOException {
      javaFile.writeTo(new File(path));
    }

    @Override public boolean delete(File file) {
      return file.delete();
    }
  }

  static class DefaultPrinter implements Printer {
    @Override public void print(String text) {
      System.out.println(text);
    }
  }
}
