plugins {
  alias(libs.plugins.convention.ksp)
  alias(libs.plugins.convention.maven.publish)
}

dependencies {
  api(project(":core"))

  api(libs.arcadedb.engine)
  api(libs.arcadedb.studio)
  // GraphQL 能力依赖 GraalVM Truffle（引入一堆警告），当前为纯 SDK 封装用不到，先注释
  // api(libs.arcadedb.graphql)

  testImplementation(libs.arcadedb.server)
  testImplementation(libs.arcadedb.ha.raft)
}

tasks.test {
  jvmArgs(
    // ArcadeDB GraphQL 依赖 GraalVM Truffle；无 GraalVM JDK 时静音 interpreter-only 警告
    "-Dpolyglot.engine.WarnInterpreterOnly=false",
    // JDK 受限方法警告（Truffle native access / System::load）
    "--enable-native-access=ALL-UNNAMED",
    // JUnit 建议的新执行器
    "-Djunit.jupiter.execution.parallel.config.executor-service=WORKER_THREAD_POOL",
  )
}
