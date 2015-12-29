# generator.java

[![Build Status](https://travis-ci.org/contentful/generator.java.svg)](https://travis-ci.org/contentful/generator.java/builds#)

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
  <version>0.9.3</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.contentful.generator:generator:0.9.3'
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
 [2]: https://search.maven.org/remote_content?g=com.contentful.generator&a=generator&v=LATEST
 [3]: https://contentful.github.io/generator.java/javadoc
 [4]: https://raw.githubusercontent.com/contentful/contentful-management.java/master/LICENSE.txt
 
