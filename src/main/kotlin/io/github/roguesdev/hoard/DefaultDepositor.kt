package io.github.roguesdev.hoard

import io.github.roguesdev.hoard.serialization.Serializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Type

internal class DefaultDepositor<T>(
  private val serializer: Serializer,
  directory: File,
  key: String,
  private val type: Type
) : Depositor<T> {

  private val saveFile: File = File(directory, key)

  @Synchronized
  override fun store(value: T?) {
    if (value == null) {
      delete()
      return
    }

    return FileOutputStream(saveFile).use {
      serializer.serialize<Any>(type, value, it)
    }
  }

  @Synchronized
  override fun retrieve(): T? {
    if (!saveFile.exists()) {
      return null
    }

    return FileInputStream(saveFile).use {
      serializer.deserialize<T>(type, it)
    }
  }

  @Synchronized
  override fun delete() {
    if (!saveFile.exists()) return
    saveFile.delete()
  }

  @Synchronized
  override fun exists(): Boolean {
    return saveFile.exists()
  }
}
