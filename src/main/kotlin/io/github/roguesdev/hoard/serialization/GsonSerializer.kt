package io.github.roguesdev.hoard.serialization

import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import io.github.roguesdev.hoard.UTF_8
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.lang.reflect.Type

/**
 * Adapter for saving values as json using Gson.
 *
 * @since 1.0
 */
class GsonSerializer(private val gson: Gson = Gson()) : Serializer {

  @Throws(IOException::class)
  override fun <T> serialize(type: Type, value: T, outputStream: OutputStream) {
    val outputStreamWriter = OutputStreamWriter(outputStream, UTF_8)
    val jsonWriter = JsonWriter(outputStreamWriter)

    gson.toJson(value, type, jsonWriter)

    jsonWriter.close()
    outputStreamWriter.close()
  }

  @Throws(IOException::class)
  override fun <T> deserialize(type: Type, inputStream: InputStream): T = InputStreamReader(inputStream, UTF_8).use {
    gson.fromJson(it, type)
  }

  internal companion object {
    init {
      try {
        Class.forName("com.google.gson.Gson", false, GsonSerializer::class.java.classLoader)
      } catch (e: ClassNotFoundException) {
        throw IllegalStateException("Gson is not found on the classpath. Please include the Gson library in order to use GsonSerializer")
      }
    }
  }
}
