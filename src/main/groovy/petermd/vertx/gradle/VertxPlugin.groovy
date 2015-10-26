package petermd.vertx.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Zip

class VertxPlugin implements Plugin<Project> {

  void apply(Project project) {

    project.extensions.create("vertx", VertxConvention)
    project.extensions.vertx.extensions.create("modules", VertxModules)

    project.afterEvaluate {

      defineConfigurations(project)
      addDependencies(project)

      VertxConvention mod = project.extensions.vertx

      mod.id = project.name
      mod.groupId = project.group.toString()
      mod.version = project.version

      project.task("localMod", type: Copy, dependsOn: 'classes', description: 'local mod') {
        into "build/local/${mod.fqName()}"
        from 'src/main/mod'
      }

      project.task("installMod", type: Copy, dependsOn: 'classes', description: 'install mod') {
        into "build/mods/${mod.fqName()}"
        from project.tasks.compileJava
        // Include src/main/resources
        from 'src/main/resources'
        // Package compile classpath excepting the provided dependencies
        into('lib') {
          from project.configurations.compile - project.configurations.provided
        }
      }

      project.task("installDeps", type: JavaExec, dependsOn: ['classes', 'localMod'], description: 'install module deps') {

        // Always execute
        outputs.upToDateWhen { false }

        main = "org.vertx.java.platform.impl.cli.Starter"

        systemProperties = [
          "vertx.mods"                                  : new File("build/mods").absolutePath,
          "org.vertx.logger-delegate-factory-class-name": "org.vertx.java.core.logging.impl.SLF4JLogDelegateFactory"
        ]

        args = ["pulldeps", "${mod.fqName()}"].flatten()

        logger.info("args=[" + args.join('|') + "]")

        classpath = project.sourceSets.main.runtimeClasspath
        workingDir = "build/local/${mod.fqName()}"
      }

      project.task("zipMod", type: Zip, dependsOn: 'classes', description: 'create module artifact') {
        archiveName = "${mod.fqName()}.zip"
        from project.tasks.compileJava
        // Include src/main/resources
        from 'src/main/resources'
        // Package compile classpath excepting the provided dependencies
        into('lib') {
          from project.configurations.compile - project.configurations.provided
        }
      }

      project.task("runMod", type: JavaExec, dependsOn: ['classes','localMod'], description: 'run module') {

        // Always execute
        outputs.upToDateWhen { false }

        main="org.vertx.java.platform.impl.cli.Starter"

        systemProperties=[
          "vertx.mods":new File("build/mods").absolutePath,
          "org.vertx.logger-delegate-factory-class-name":"org.vertx.java.core.logging.impl.SLF4JLogDelegateFactory",
          "log4j.debug":"true"
        ]

        // Map -Dargs="" into the command-line
        def clArgs=project.hasProperty('args')?project.args.split('\\s+'):[]

        args=[ "runmod", "${mod.fqName()}" ].plus(clArgs).flatten()

        logger.info("clArgs=["+clArgs.join('|')+"]")
        logger.info("args=["+args.join('|')+"]")

        classpath=project.sourceSets.test.runtimeClasspath
        workingDir="build"
      }

      defineArtifact(project)
    }

    // Create a new vertx scaffold
    project.task('initMod') << {

      VertxConvention mod = project.extensions.vertx

      // If no languages are specified then infer from the plugins
      if (mod.languages.isEmpty()) {
        mod.languages << getBaseLanguage(project)
      }

      project.logger.info("Creating vertx")

      // vertx.json
      new ModScaffold(project, mod).create()

      // languages
      mod.languages.eachWithIndex { String lang, int idx ->

        project.logger.info("Adding language '$lang'")

        LanguageScaffold scaffold;

        switch (lang) {
          case "java":
            scaffold = new JavaScaffold(project, mod);
            break;
          case "groovy":
            scaffold = new GroovyScaffold(project, mod);
            break;
          case "scala":
            scaffold = new LanguageScaffold(project, mod, "scala");
            break;
          case "python":
            scaffold = new LanguageScaffold(project, mod, "python");
            break;
          case "ruby":
            scaffold = new LanguageScaffold(project, mod, "ruby");
            break;
          case "kotlin":
            scaffold = new LanguageScaffold(project, mod, "kotlin");
            break;
          default:
            project.logger.error("Unrecognised langauge '${lang}'")
            break
        }

        scaffold.create()

        // Generate a main() for the first language
        if (idx == 0) {
          scaffold.addMain()
        }
      }

      // templates
      mod.templates.eachWithIndex { String template, int idx ->

        project.logger.info("Applying template '$template'")

        Scaffold scaffold

        switch (template) {
          case "web":
            scaffold = new WebScaffold(project, mod)
            break
          default:
            project.logger.error("Unrecognised template '${template}'")
            break
        }

        scaffold.create()
      }

    }
  }

  // Implementation

  /** Define configuration */
  void defineConfigurations(Project project) {

    project.logger.debug("Add new configuration 'provided'")

    // Create provided configuration
    Configuration providedConfiguration = project.configurations
      .create("provided")
      .setDescription("Configuration for platform provided dependencies");

    // Include in the Java 'compile' configuration
    project.configurations.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(providedConfiguration);
  }

  void addDependencies(Project project) {

    VertxConvention mod = project.extensions.vertx

    // Platform dependencies
    project.dependencies.add('provided', "io.vertx:vertx-core:${mod.platformVersion}");
    project.dependencies.add('provided', "io.vertx:vertx-platform:${mod.platformVersion}");
    // TBD: Depending on lang array - apply the appropriate jar to provided classpath for runMod
    project.dependencies.add('provided', "io.vertx:lang-groovy:${mod.platformVersion}");

    project.dependencies.add('testCompile', "junit:junit:${mod.junitVersion}");
    project.dependencies.add('testCompile', "io.vertx:testtools:${mod.testToolsVersion}");
    // Add logging for runMod
    project.dependencies.add('provided', "org.slf4j:slf4j-log4j12:1.6.+")
    project.dependencies.add('provided', "log4j:log4j:1.2.+")

    // Add all non-runnable modules to dependencies
    project.extensions.vertx.modules.requireModules.each { m -> project.dependencies.add('provided', m) }
  }

  /** Define artifact */
  void defineArtifact(Project project) {
    VertxConvention mod = project.extensions.vertx

    project.artifacts { archives project.tasks.zipMod }
  }

  /** Infer the base language of the project */
  String getBaseLanguage(Project project) {

    String lang;
    project.plugins.each() { p ->
      if (p instanceof JavaPlugin) {
        // Java is included by other language plugins so only set as default
        lang = lang ?: "java"
      } else if (p instanceof GroovyPlugin) {
        lang = "groovy"
      } else if (p instanceof ScalaPlugin) {
        lang = "scala"
      }
    }

    project.logger.info("Base language is '${lang}'")

    return lang;
  }
}
