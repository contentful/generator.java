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

      if (line.hasOption("space")
          && line.hasOption("package")
          && line.hasOption("folder")
          && line.hasOption("token")) {

        new Generator().generate(line.getOptionValue("space"), line.getOptionValue("token"),
            line.getOptionValue("package"), line.getOptionValue("folder"));
      } else {
        usage(args[0], options);
      }
    } catch (ParseException e) {
      System.err.println("Parsing failed, reason: " + e.getMessage());
    }
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
