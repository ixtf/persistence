package com.github.ixtf.persistence.arcade

import com.github.ixtf.persistence.arcade.domain.Person
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JarcadeTest {
  private val component = DaggerTestAppComponent.factory().create()

  @Test
  fun dd() = runBlocking {
    val repository = component.personRepository

    // 新增
    val alice = Person(name = "Alice", age = 32)
    repository.save(alice)
    assertEquals(alice, repository.findOrNull(alice.id))

    // 更新
    alice.age = 33
    repository.save(alice)
    assertEquals(33, repository.find(alice.id).age)

    // 列表
    val bob = Person(name = "Bob", age = 28)
    repository.save(bob)
    val all = repository.list().toList()
    assertEquals(setOf(alice, bob), all.toSet() intersect setOf(alice, bob))

    // 按 id 批量查
    val byIds = repository.listByIds(listOf(alice.id, bob.id)).toList()
    assertEquals(setOf(alice, bob), byIds.toSet())
  }
}
