package petermd.vertx.gradle

import org.gradle.api.Project

/** Module Scaffold */
class ModScaffold extends Scaffold {

  public ModScaffold(Project project, VertxConvention mod) {
    super(project, mod)
  }

  void create() {
    File srcResources = autoCreatePath("src/main/resources");
    generate(new File(srcResources, "mod.json"), { w ->
      def allMods = vertx.modules.all()
      w.println("{");
      w.println("\t\"includes\":\"" + allMods.join(",") + "\",");
      w.println("\t\"description\":\"tbd\",")
      w.println("\t\"licenses\":[\"tbd\"],")
      w.println("\t\"author\":\"tbd\"")
      w.println("}");
    });
  }
}
