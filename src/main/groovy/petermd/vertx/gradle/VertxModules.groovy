package petermd.vertx.gradle

import sun.net.www.content.audio.x_aiff

/** Modules */
class VertxModules {

  def requireModules = [];
  def includeModules = [];

  /** Return all modules */
  List<String> all() {
    return requireModules + includeModules
  }

  /** Non-runnable modules */
  void require(String ref) {
    requireModules << ref
  }

  /** Runnable modules */
  void include(String ref) {
    includeModules << ref
  }
}
