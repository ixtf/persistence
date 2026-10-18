@file:Suppress("unused")

package com.github.ixtf.persistence.mongo.internal.api

import cn.hutool.core.util.ReUtil
import com.github.ixtf.core.J
import com.github.ixtf.core.kit.date
import com.github.ixtf.core.kit.ld
import com.mongodb.client.model.Filters.*
import com.mongodb.kotlin.client.model.Filters.eq
import com.mongodb.kotlin.client.model.Filters.gte
import com.mongodb.kotlin.client.model.Filters.`in`
import com.mongodb.kotlin.client.model.Filters.lte
import com.mongodb.kotlin.client.model.Filters.or
import com.mongodb.kotlin.client.model.Filters.regex
import java.util.*
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE
import kotlin.reflect.KProperty
import org.bson.conversions.Bson

@JvmSynthetic
@JvmName("eqOrNullExt")
infix fun <T> KProperty<T?>.eqOrNull(v: T?): Bson? =
  if (v == null) null else if (v is CharSequence && v.isBlank()) null else eq(v)

@Suppress("FunctionNaming")
@JvmSynthetic
@JvmName("inOrNullExt")
infix fun <T> KProperty<T?>.inOrNull(v: Iterable<T?>?): Bson? = if (J.isEmpty(v)) null else `in`(v)

@Suppress("UNCHECKED_CAST")
@JvmSynthetic
@JvmName("gteOrNullExt")
infix fun <T> KProperty<T>.gteOrNull(v: T?): Bson? =
  if (v == null) null else if (v is Date) gte(v.ld().date() as T) else gte(v)

@Suppress("UNCHECKED_CAST")
@JvmSynthetic
@JvmName("lteOrNullExt")
infix fun <T> KProperty<T>.lteOrNull(v: T?): Bson? =
  if (v == null) null else if (v is Date) lte(v.ld().plusDays(1).date() as T) else lte(v)

@JvmSynthetic
@JvmName("regexOrNullExt")
infix fun KProperty<String?>.regexOrNull(pattern: Pattern?): Bson? = pattern?.let { regex(it) }

@JvmSynthetic
@JvmName("regexOrNullExt")
infix fun KProperty<String?>.regexOrNull(v: String?): Bson? =
  v?.takeIf { it.isNotBlank() }?.let { regex("/${ReUtil.escape(it)}/") }

@JvmSynthetic
@JvmName("regexIgnoreCaseOrNullExt")
infix fun KProperty<String?>.regexIgnoreCaseOrNull(v: String?): Bson? =
  v?.takeIf { it.isNotBlank() }
    ?.let { Pattern.compile(ReUtil.escape(it), CASE_INSENSITIVE) }
    ?.let { regex(it) }

@JvmSynthetic
@JvmName("orOrNullExt")
fun orOrNull(filters: Iterable<Bson>?): Bson? = if (J.isEmpty(filters)) null else or(filters!!)

@JvmSynthetic
@JvmName("orOrNullExt")
fun orOrNull(vararg filters: Bson?): Bson? = orOrNull(filters.filterNotNull().toList())

infix fun String.regexOrNull(v: Pattern?) = v?.let { regex(this, it) }

infix fun String.regexOrNull(v: String?) =
  v?.takeIf { it.isNotBlank() }?.let { regex(this, "/${ReUtil.escape(it)}/") }

infix fun String.regexIgnoreCaseOrNull(v: String?) =
  this regexOrNull
    v?.takeIf { it.isNotBlank() }?.let { Pattern.compile(ReUtil.escape(it), CASE_INSENSITIVE) }

infix fun String.eqOrNull(v: CharSequence?) = v?.takeIf { it.isNotBlank() }?.let { eq(this, it) }

infix fun String.eqOrNull(v: Any?) = v?.let { eq(this, it) }

infix fun String.inOrNull(v: Iterable<*>?) = v?.takeIf { J.nonEmpty(it) }?.let { `in`(this, it) }

infix fun String.gteOrNull(v: Any?) = v?.let { gte(this, it) }

infix fun String.lteOrNull(v: Any?) = v?.let { lte(this, it) }
