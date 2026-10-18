pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2")
  }
}

rootProject.name = "persistence"

include("core")

include("mongo")

include("hugegraph")
include("arcade")
