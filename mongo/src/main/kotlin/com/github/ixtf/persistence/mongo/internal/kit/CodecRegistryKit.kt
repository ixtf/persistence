package com.github.ixtf.persistence.mongo.internal.kit

import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry

fun CodecRegistry.asBson(o: Any): BsonDocument =
  BsonDocument().also { bsonDocument ->
    BsonDocumentWriter(bsonDocument).use { writer ->
      get(o.javaClass).encode(writer, o, EncoderContext.builder().build())
    }
  }

inline fun <reified T : Any> CodecRegistry.fromBson(bson: BsonDocument): T =
  BsonDocumentReader(bson).use { reader ->
    get(T::class.java).decode(reader, DecoderContext.builder().build())
  }

inline fun <reified T : Any> CodecRegistry.fromBson(bson: String): T =
  fromBson(BsonDocument.parse(bson))
