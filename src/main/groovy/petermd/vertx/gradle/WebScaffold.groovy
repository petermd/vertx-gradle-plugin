package petermd.vertx.gradle;

import org.gradle.api.Project;

import java.io.File;

/** Scaffold for a WebApp */
public class WebScaffold extends Scaffold {

  public WebScaffold(Project project, VertxConvention mod) {
    super(project, mod)
  }

  void create() {
    File srcJavascript = autoCreatePath("src/web/javascript")
    File srcComponents = autoCreatePath("src/web/components")
    File srcDocuments = autoCreatePath("src/web/html")

    generate(new File(srcJavascript, "app.js"), { w ->
      def allMods = vertx.modules.all()
      w.println("""
// Javascript Magic Goes Here
""")
    })

    // Bower

    // Basic Settings
    generate(new File(project.rootDir, "bower.json"), { w ->
      w.println("""
{
  "name":"${project.name}",
  "private":true
}
""")
    })

    // Re-locate components to src/web/components
    generate(new File(project.rootDir, ".bowerrc"), { w ->
      w.println("""
{
  "directory":"src/web/components"
}
""")
    })
  }
}
