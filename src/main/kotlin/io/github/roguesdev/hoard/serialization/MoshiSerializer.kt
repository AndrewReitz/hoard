package io.github.roguesdev.hoard.serialization

import com.squareup.moshi.Moshi
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type
import okio.buffer
import okio.sink
import okio.source

/**
 * Adapter for saving values as json using Moshi.
 *
 * @since 1.0
 */
class MoshiSerializer(private val moshi: Moshi = Moshi.Builder().build()) : Serializer {

  @Throws(IOException::class)
  override fun <T> serialize(type: Type, value: T, outputStream: OutputStream) {
    val adapter = moshi.adapter<T>(type) // no need to cache moshi does this for us.
    outputStream.sink().buffer().use {
      adapter.toJson(it, value)
    }
  }

  @Throws(IOException::class)
  override fun <T> deserialize(type: Type, inputStream: InputStream): T = inputStream.source().buffer().use {
    moshi.adapter<T>(type).fromJson(it)!!
  }

  internal companion object {
    init {
      try {
        Class.forName("com.squareup.moshi.Moshi", false, MoshiSerializer::class.java.classLoader)
      } catch (e: ClassNotFoundException) {
        throw IllegalStateException("Moshi is not found on the classpath. Please include the Moshi library to use the MoshiSerializer")
      }
    }
  }
}
