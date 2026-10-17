plugins {
  alias(libs.plugins.convention.ksp)
  alias(libs.plugins.convention.maven.publish)
}

dependencies {
  api(project(":core"))
  api("jakarta.persistence:jakarta.persistence-api")
  api("org.mongodb:mongodb-driver-kotlin-coroutine")
  api("org.mongodb:mongodb-driver-kotlin-extensions")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  api("org.mongodb:bson-kotlinx")
  api("io.github.classgraph:classgraph")
  api("com.github.ben-manes.caffeine:caffeine")
  api("com.sksamuel.aedile:aedile-core")
}
