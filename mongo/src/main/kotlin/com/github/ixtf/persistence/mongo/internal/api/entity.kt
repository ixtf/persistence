@file:Suppress("unused")

package com.github.ixtf.persistence.mongo.internal.api

import com.github.ixtf.persistence.mongo.Jmongo.Companion.DELETED_COL
import com.github.ixtf.persistence.mongo.Jmongo.Companion.ID_COL
import com.mongodb.DBRef
import org.bson.BsonDocument
import org.bson.Document
import org.bson.types.ObjectId

// inline fun <reified T : MongoEntityBase> T.ref(): JmongoRef<T> =
// Jguice.get<Jmongo>().asRef<T>(id)
//
// suspend inline fun <reified T : MongoEntityBase> JmongoRef<T>.entityOrNull(): T? =
//  Jguice.get<Jmongo>().entityCollection<T>().singleOrNull(id)
//
// suspend inline fun <reified T : MongoEntityBase> JmongoRef<T>.entity(): T =
//  Jguice.get<Jmongo>().entityCollection<T>().single(id)
//
// inline fun <reified T : MongoEntityBase> T.asBson() =
//  Jguice.get<Jmongo>().entityCollection<T>().asBson(this)
//
// inline fun <reified T : MongoEntityBase> BsonDocument.asEntity(): T =
//  Jguice.get<Jmongo>().entityCollection<T>().fromBson(this)
//
// inline fun <reified T : MongoEntityBase> Document.asEntity(): T = toBsonDocument().asEntity()
//
// inline fun <reified T : MongoEntityBase> DBRef.ref(): JmongoRef<T> =
//  Jguice.get<Jmongo>().asRef<T>(id as String)

fun BsonDocument.id(): String = getString(ID_COL).value

fun BsonDocument.deleted(): Boolean = getBoolean(DELETED_COL).value

fun BsonDocument.objectId(key: Any = ID_COL): ObjectId = getObjectId(key).value

fun Document.id(): String = getString(ID_COL) ?: getString("id")

fun Document.deleted(): Boolean = getBoolean(DELETED_COL, false)

fun Document.objectId(key: Any = ID_COL): ObjectId = getObjectId(key)

fun Document.dbRefOrNull(key: Any): DBRef? = get(key, DBRef::class.java)

fun Document.dbRef(key: Any): DBRef = dbRefOrNull(key)!!

fun Document.dbRefsOrNull(key: Any): List<DBRef>? = getList(key, DBRef::class.java)

fun Document.dbRefs(key: Any): List<DBRef> = dbRefsOrNull(key)!!
