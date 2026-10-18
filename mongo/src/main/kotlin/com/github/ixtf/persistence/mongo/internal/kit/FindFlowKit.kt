package com.github.ixtf.persistence.mongo.internal.kit

import com.github.ixtf.persistence.Sort
import com.github.ixtf.persistence.SortStart
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.FindFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson

suspend fun <T : Any> FindFlow<T>.sort(sort: Flow<Sort>?): FindFlow<T> = sort(sort?.asSort())

suspend fun <T : Any> FindFlow<T>.sort(vararg sort: Sort) = sort(sort.asFlow())

suspend fun <T : Any> FindFlow<T>.sort(sort: Iterable<Sort>?) = sort(sort?.asFlow())

suspend fun Flow<Sort>.asSort(): Bson? =
  filterNotNull()
    .distinctUntilChangedBy { it.id }
    .toList()
    .map { if (it.start == SortStart.asc) Sorts.ascending(it.id) else Sorts.descending(it.id) }
    .let {
      when (it.size) {
        0 -> null
        1 -> it.first()
        else -> Sorts.orderBy(it)
      }
    }
