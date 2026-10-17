plugins {
  alias(libs.plugins.convention.ksp)
  alias(libs.plugins.convention.maven.publish)
}

dependencies { api("io.github.ixtf:vertx") }
