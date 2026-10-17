package com.gitee.ixtf.mongo.kotlinx

import com.github.ixtf.core.kit.inputCommand
import com.github.ixtf.core.kit.readJson
import org.bson.types.Code

inline fun <reified T> Code.readJson(): T = code.readJson()

inline fun <reified T> Code.inputCommand(): T = code.inputCommand()
