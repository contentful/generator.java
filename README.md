# generator.java

[![Build Status](https://travis-ci.org/contentful/generator.java.svg)](https://travis-ci.org/contentful/generator.java/builds#)
[![codecov](https://codecov.io/gh/mariobodemann/generator.java/branch/master/graph/badge.svg)](https://codecov.io/gh/mariobodemann/generator.java)

Code generator for Contentful models.

> [Contentful][1] is a content management platform for web applications, mobile apps and connected devices. It allows you to create, edit & manage content in the cloud and publish it anywhere via powerful API. Contentful offers tools for managing editorial teams and enabling cooperation between organizations.

This tool can be used to fetch content types from a Contentful space, and generate corresponding java source files.

Link / Array fields that are restricted to *exactly* one content type (via field validation) will reference the generated class corresponding to that content type.

Setup
=====

Download [the latest JAR][2] or grab via Maven:
```xml
<dependency>
  <groupId>com.contentful.generator</groupId>
  <artifactId>generator</artifactId>
  <version>1.1.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.contentful.generator:generator:1.1.0'
```

Usage
=====

## From the command line:

```
usage: generator.java
 -f,--folder <arg>    Destination folder path
 -p,--package <arg>   Destination package name
 -s,--space <arg>     Space ID
 -t,--token <arg>     Management API Access Token
```

The tool can also be used directly from code, refer to the [documentation][3] for more info.

License
=======

Copyright (c) 2015 Contentful GmbH. See [LICENSE][4] for further details.


 [1]: https://www.contentful.com
 [2]: http://search.maven.org/remotecontent?filepath=com/contentful/generator/generator/1.1.0/generator-1.1.0-jar-with-dependencies.jar
 [3]: https://www.contentful.com/developers/docs/android/tutorials/offline-persistence-with-vault/#generatorjava
 [4]: https://raw.githubusercontent.com/contentful/contentful-management.java/master/LICENSE.txt
 
