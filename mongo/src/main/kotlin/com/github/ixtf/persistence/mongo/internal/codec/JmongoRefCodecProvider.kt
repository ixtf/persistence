package com.github.ixtf.persistence.mongo.internal.codec

import com.github.ixtf.persistence.mongo.MongoEntity
import com.github.ixtf.persistence.mongo.internal.MongoRuntimeModuleConfig
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

@Deprecated("use id")
data class JmongoRef<T : MongoEntity>(
  val databaseName: String? = null,
  val collectionName: String,
  val id: String,
)

@Deprecated("use id")
internal class JmongoRefCodecProvider(private val config: MongoRuntimeModuleConfig) :
  CodecProvider {
  @Suppress("UNCHECKED_CAST")
  override fun <T> get(clazz: Class<T>, registry: CodecRegistry?): Codec<T>? =
    if (clazz == JmongoRef::class.java) JmongoRefCodec(config) as Codec<T> else null
}

@Deprecated("use id")
internal class JmongoRefCodec(config: MongoRuntimeModuleConfig) : Codec<JmongoRef<*>> {
  private val defaultDatabaseName = config.database

  override fun getEncoderClass() = JmongoRef::class.java

  override fun encode(writer: BsonWriter, value: JmongoRef<*>?, encoderContext: EncoderContext) {
    value?.apply {
      writer.writeStartDocument()
      databaseName
        ?.takeIf { defaultDatabaseName != it }
        ?.let { writer.writeString(DATABASE_NAME_COL, it) }
      writer.writeString(COLLECTION_NAME_COL, collectionName)
      writer.writeString(ID_COL, id)
      writer.writeEndDocument()
    }
  }

  override fun decode(reader: BsonReader, decoderContext: DecoderContext): JmongoRef<*> {
    var databaseName: String? = null
    var collectionName: String? = null
    var id: String? = null
    reader.readStartDocument()
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      val k = reader.readName()
      val v = reader.readString()
      when (k) {
        DATABASE_NAME_COL -> databaseName = v
        COLLECTION_NAME_COL -> collectionName = v
        ID_COL -> id = v
        else -> throw CodecConfigurationException("decode： $encoderClass for $k")
      }
    }
    reader.readEndDocument()
    return JmongoRef<MongoEntity>(
      databaseName = databaseName,
      collectionName = requireNotNull(collectionName),
      id = requireNotNull(id),
    )
  }

  companion object {
    private const val DATABASE_NAME_COL = "\$db"
    private const val COLLECTION_NAME_COL = "\$ref"
    private const val ID_COL = "\$id"
  }
}
