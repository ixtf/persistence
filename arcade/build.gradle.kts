plugins {
  alias(libs.plugins.convention.ksp)
  alias(libs.plugins.convention.maven.publish)
}

dependencies {
  api(project(":core"))

  api(libs.arcadedb.engine)
  api(libs.arcadedb.studio)
  api(libs.arcadedb.graphql)

  testImplementation(libs.arcadedb.server)
  testImplementation(libs.arcadedb.ha.raft)
}
