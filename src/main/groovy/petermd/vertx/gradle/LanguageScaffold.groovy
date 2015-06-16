package petermd.vertx.gradle

import org.gradle.api.Project

/** Generic Language Scaffold */
class LanguageScaffold extends Scaffold {

  String name

  File srcPath, testPath

  LanguageScaffold(Project project, VertxConvention mod, String name) {
    super(project, mod)
    this.name = name
  }

  void create() {
    this.srcPath = autoCreatePath("src/main/${name}");
    this.testPath = autoCreatePath("src/test/${name}");
  }

  void addMain() {
  }
}
