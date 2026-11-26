plugins {
  alias(libs.plugins.convention.spotless)
  alias(libs.plugins.convention.kotlin.jvm)
  alias(libs.plugins.convention.ksp)
  alias(libs.plugins.convention.maven.publish)
}

dependencies {
  api("io.github.ixtf:core")
}
