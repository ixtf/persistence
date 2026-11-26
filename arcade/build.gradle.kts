plugins {
  alias(libs.plugins.convention.spotless)
  alias(libs.plugins.convention.kotlin.jvm)
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

// cqrs/compiler 传递依赖 io.github.ixtf.persistence:arcade，
// 与本模块坐标相同，会被 Gradle 替换为自身 jar，
// 导致 kspTestKotlin 隐式依赖校验失败，无法编译/运行测试。
// TODO: 根治方案是让 gradle 仓库的 cqrs 不再依赖 persistence:arcade（依赖倒置）
configurations.all {
  exclude(group = "io.github.ixtf.persistence", module = "arcade")
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
