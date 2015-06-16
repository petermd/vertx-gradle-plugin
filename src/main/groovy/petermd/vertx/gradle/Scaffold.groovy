package petermd.vertx.gradle

import org.gradle.api.Project

/** Base Scaffold */
class Scaffold {

  final Project project
  final VertxConvention vertx

  public Scaffold(Project project, VertxConvention vertx) {
    this.project = project
    this.vertx = vertx
  }

  void create() {
  }

  File autoCreatePath(String path) {
    File loc = project.file(path);
    if (!loc.exists()) {
      if (!loc.mkdirs()) {
        throw new RuntimeException("Unable to create source path " + loc);
      }
    }
    return loc;
  }

  void generate(File file, Closure c) {
    if (file.exists())
      return;
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    PrintWriter w = null;
    try {
      w = file.newPrintWriter();
      c(w);
    }
    finally {
      w.close();
    }
  }
}
