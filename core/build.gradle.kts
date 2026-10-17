plugins {
  alias(libs.plugins.convention.ksp)
  alias(libs.plugins.convention.maven.publish)
}

dependencies {
  api("io.github.ixtf:core")
  api("com.google.guava:guava")
  api("jakarta.validation:jakarta.validation-api")
}
