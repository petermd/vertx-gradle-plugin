package petermd.vertx.gradle

/** Vertx Extension Object */
class VertxConvention {

  String id
  String groupId
  String version

  String main

  String platformVersion
  Iterable<String> languages = []
  Iterable<String> templates = []

  String junitVersion = "4.+"
  String testToolsVersion = "2.+"

  String toString() {
    return "VertxConvention " + groupId + "~" + id + "~" + version;
  }

  void language(String name, String version = "*") {
    this.languages << name
  }

  void template(String name) {
    this.templates << name
  }

  String fqName() {
    return "${groupId}~${id}~${version}"
  }
}
