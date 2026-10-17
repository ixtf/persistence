package com.gitee.ixtf.mongo.codec

import com.github.ixtf.persistence.IOperator
import com.github.ixtf.persistence.dto.DefaultOperator
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecConfigurationException
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

@Deprecated("user DefaultOperator")
object IOperatorCodecProvider : CodecProvider {
  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry?): Codec<T>? =
    clazz
      .takeIf { clazz == IOperator::class.java || clazz == DefaultOperator::class.java }
      ?.let { IOperatorCodec as Codec<T> }
}

@Deprecated("user DefaultOperator")
object IOperatorCodec : Codec<IOperator> {
  private const val ID_COL = "id"
  private const val OLD_ID_COL = "_id"
  private const val NAME_COL = "name"

  override fun getEncoderClass() = IOperator::class.java

  override fun encode(writer: BsonWriter, value: IOperator?, encoderContext: EncoderContext) {
    value?.apply {
      writer.writeStartDocument()
      writer.writeString(ID_COL, id)
      writer.writeString(NAME_COL, name)
      writer.writeEndDocument()
    }
  }

  override fun decode(reader: BsonReader, decoderContext: DecoderContext): IOperator {
    var id: String? = null
    var name: String? = null
    reader.readStartDocument()
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      val k = reader.readName()
      val v = reader.readString()
      when (k) {
        ID_COL,
        OLD_ID_COL -> id = v
        NAME_COL -> name = v
        else -> throw CodecConfigurationException("decode： $encoderClass for $k")
      }
    }
    reader.readEndDocument()
    return DefaultOperator(id!!, name!!)
  }
}
