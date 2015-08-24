package org.mechaverse.simulation.common.util;

import com.google.common.base.Strings;

import java.io.PrintWriter;

/**
 * A {@link PrintWriter} wrapper that supports indentation.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class IndentPrintWriter {

  private final PrintWriter out;
  private int indent = 0;
  private String indentSpaces = "";
  private int spacesPerIndent = 2;

  public IndentPrintWriter(final PrintWriter out) {
    this.out = out;
  }

  public IndentPrintWriter printf(String format, Object... args) {
    out.printf(indent(format), args);
    return this;
  }

  public IndentPrintWriter println(String str) {
    out.println(indent(str));
    return this;
  }

  public IndentPrintWriter indent() {
    indent++;
    indentSpaces = Strings.repeat(" ", indent * spacesPerIndent);
    return this;
  }

  public IndentPrintWriter unindent() {
    indent--;
    indentSpaces = Strings.repeat(" ", indent * spacesPerIndent);
    return this;
  }

  public IndentPrintWriter println() {
    out.println();
    return this;
  }

  public void flush() {
    out.flush();
  }

  private String indent(String str) {
    return str.replaceAll("(?m)^", indentSpaces);
  }

  public void printLines(String lines) {
    for (String line : lines.split("\r?\n")) {
      println(line.trim());
    }
  }
}
