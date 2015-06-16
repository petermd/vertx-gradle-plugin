package petermd.vertx.gradle

import org.gradle.api.Project

/** Java Scaffold */
class JavaScaffold extends LanguageScaffold {

  JavaScaffold(Project project, VertxConvention mod) {
    super(project, mod, "java")
  }

  void addMain() {
    def vertPackage = vertx.groupId + "." + vertx.id;
    def packageDir = new File(srcPath, vertPackage.replace('.', '/'));

    generate(new File(packageDir, "Main.java"), { w ->
      w.println("""
package ${vertPackage};

import org.vertx.java.platform.Verticle;

public class Main extends Verticle {
    public void start() {
        // Java magic starts here
    }
}
""");
    });
  }
}
