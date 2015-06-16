package petermd.vertx.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class TestVertxPlugin {

  @Test
  public void pluginInstallsOk() {
    Project project = ProjectBuilder.builder().build()
    project.apply plugin: 'vertx'
  }

}
