package test.arcadedb

import com.arcadedb.database.DatabaseFactory

private val factory = DatabaseFactory("/tmp/arcadedb/mydb")
private val database = if (factory.exists()) factory.open() else factory.create()

fun main() {
  factory.use {
    database.use { db ->
      db.transaction {
        // Define schema
        db.schema.getOrCreateVertexType("Person")
        db.schema.getOrCreateEdgeType("Knows")

        // Create vertices
        val alice = db.newVertex("Person").set("name", "Alice").set("age", 32).save()
        val bob = db.newVertex("Person").set("name", "Bob").set("age", 28).save()

        // Create edge
        alice.newEdge("Knows", bob).set("since", 2020).save()
      }
    }
  }
}
