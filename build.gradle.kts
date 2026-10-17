plugins {
  alias(libs.plugins.convention.spotless)
  alias(libs.plugins.convention.kotlin.jvm) apply false
  alias(libs.plugins.convention.maven.publish) apply false
  alias(libs.plugins.convention.ksp) apply false
}
