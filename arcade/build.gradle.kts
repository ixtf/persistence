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

// cqrs/compiler 传递依赖 io.github.ixtf.persistence:arcade，
// 与本模块坐标相同，会被 Gradle 替换为自身 jar，
// 导致 kspTestKotlin 隐式依赖校验失败，无法编译/运行测试。
// TODO: 根治方案是让 gradle 仓库的 cqrs 不再依赖 persistence:arcade（依赖倒置）
configurations.all {
  exclude(group = "io.github.ixtf.persistence", module = "arcade")
}
