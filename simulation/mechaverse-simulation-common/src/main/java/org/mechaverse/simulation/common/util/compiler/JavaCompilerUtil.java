package org.mechaverse.simulation.common.util.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Utility methods for compiling Java source code.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class JavaCompilerUtil {

  private JavaCompilerUtil() {}

  /**
   * An exception which indicates that a compilation error occurred.
   */
  public static class CompileException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public CompileException(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
      this.diagnostics = diagnostics;
    }

    public CompileException(List<Diagnostic<? extends JavaFileObject>> diagnostics,
        Throwable cause) {
      super(cause);
      this.diagnostics = diagnostics;
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
      return diagnostics;
    }
  }

  /**
   * Java source code that is stored as a String in memory.
   */
  private static class JavaSourceFromString extends SimpleJavaFileObject {

    final String code;

    public JavaSourceFromString(String name, String code) {
      super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }

  /**
   * A Java compiled class that is stored in memory.
   */
  private static class JavaClassObject extends SimpleJavaFileObject {

    protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    public JavaClassObject(String name, Kind kind) {
      super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    public byte[] getBytes() {
      return bos.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
      return bos;
    }
  }

  /**
   * A memory based Java file manager.
   */
  private static class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private JavaClassObject jclassObject;

    public ClassFileManager(StandardJavaFileManager standardManager) {
      super(standardManager);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
      return new SecureClassLoader() {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
          byte[] b = jclassObject.getBytes();
          return super.defineClass(name, jclassObject.getBytes(), 0, b.length);
        }
      };
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
        FileObject sibling) throws IOException {
      jclassObject = new JavaClassObject(className, kind);
      return jclassObject;
    }
  }

  /**
   * Compiles the Java source code contained in a String.
   *
   * @param implClass the fully qualified package and name of the class that will be compiled
   * @param sourceStr the source code as a String
   *
   * @return an instance of the compiled class
   * @throws CompileException if an error occurs during compilation
   */
  @SuppressWarnings("unchecked")
  public static <T> T compile(String implClass, String sourceStr) throws CompileException {
    JavaFileObject file = new JavaSourceFromString(implClass, sourceStr);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

    JavaFileManager fileManager =
        new ClassFileManager(compiler.getStandardFileManager(null, null, null));
    CompilationTask task =
        compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

    boolean success = task.call();
    if (success) {
      try {
        return (T) fileManager.getClassLoader(null).loadClass(implClass).newInstance();
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        throw new CompileException(diagnostics.getDiagnostics(), e);
      }
    } else {
      throw new CompileException(diagnostics.getDiagnostics());
    }
  }
}
