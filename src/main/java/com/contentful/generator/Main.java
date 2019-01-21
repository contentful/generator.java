/*
 * Copyright (C) 2017 Contentful GmbH
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
  public static void main(String[] args) {
    CommandLineParser parser = new GnuParser();
    Options options = constructOptions();
    try {
      CommandLine line = parser.parse(options, args);

      final String environment = line.hasOption("environment") ? line.getOptionValue("environment") : "master";

      if (line.hasOption("space")
          && line.hasOption("package")
          && line.hasOption("folder")
          && line.hasOption("token")) {

        new Generator().generate(
            line.getOptionValue("space"),
            environment,
            line.getOptionValue("package"),
            line.getOptionValue("folder"),
            line.getOptionValue("token"));
      } else {
        usage(options);
      }
    } catch (ParseException e) {
      System.err.println("Parsing failed, reason: " + e.getMessage());
    }
  }

  public static Options constructOptions() {
    return new Options()
        .addOption("s", "space", true, "Space ID")
        .addOption("e", "environment", true, "Environment ID, 'master' if not set.")
        .addOption("t", "token", true, "Management API Access Token")
        .addOption("p", "package", true, "Destination package name")
        .addOption("f", "folder", true, "Destination folder path");
  }

  public static void usage(Options options) {
    new HelpFormatter().printHelp("generator.java", options);
  }
}
