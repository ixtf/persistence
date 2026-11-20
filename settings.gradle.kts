pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
}

rootProject.name = "persistence"

include("core")

include("mongo")

include("hugegraph")

include("arcade")
