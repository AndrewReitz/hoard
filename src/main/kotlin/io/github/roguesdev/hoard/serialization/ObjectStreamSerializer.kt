package io.github.roguesdev.hoard.serialization

import io.github.roguesdev.hoard.Hoard
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.Serializable
import java.lang.reflect.Type

/**
 * Uses [ObjectOutputStream] and [ObjectInputStream] to store objects. All
 * objects to be saved must implement [Serializable].
 * This is the default implementation for [Hoard].
 *
 * @since 1.0
 */
class ObjectStreamSerializer : Serializer {

  @Throws(IOException::class)
  override fun <T> serialize(type: Type, value: T, outputStream: OutputStream) {
    ObjectOutputStream(outputStream).use {
      it.writeObject(value)
    }
  }

  @Throws(IOException::class)
  override fun <T> deserialize(type: Type, inputStream: InputStream): T =
    ObjectInputStream(inputStream).use {
      val `object` = it.readObject()
      @Suppress("UNCHECKED_CAST")
      `object` as T
    }
}
