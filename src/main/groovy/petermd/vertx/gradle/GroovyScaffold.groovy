package petermd.vertx.gradle

import org.gradle.api.Project

/** Groovy Scaffold */
class GroovyScaffold extends LanguageScaffold {

  GroovyScaffold(Project project, VertxConvention mod) {
    super(project, mod, "groovy")
  }

  void addMain() {
    def vertPackage = vertx.groupId + "." + vertx.id;
    def packageDir = new File(srcPath, vertPackage.replace('.', '/'));

    generate(new File(packageDir, "Main.groovy"), { w ->
      w.println("""
package ${vertPackage};

import org.vertx.java.platform.Verticle;

class Main extends Verticle {
    public void start() {
        // Groovy magic starts here
    }
}
""");
    });
  }
}
