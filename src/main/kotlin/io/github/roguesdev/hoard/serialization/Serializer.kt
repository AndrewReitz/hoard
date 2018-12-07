package io.github.roguesdev.hoard.serialization

import io.github.roguesdev.hoard.Hoard
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * Specification that describes how [Hoard] should serialize objects.
 *
 * @since 1.0
 */
interface Serializer {
    /**
     * Serialize an object to the output stream.
     *
     * @param type The type of the value being serialized.
     * @param value The value to serialize to outputStream.
     * @param outputStream The stream to serialize the object to.
     * @param T The type of the object passed in as value
     * @throws IOException if operations on outputStream fail.
     */
    @Throws(IOException::class)
    fun <T> serialize(type: Type, value: T, outputStream: OutputStream)

    /**
     * De-serialize an object to the input stream.
     *
     * @param type The type of the object to de-serialize from the inputStream.
     * @param inputStream The stream to read the object from.
     * @param T The type of the object to return as.
     * @return The de-serialized value.
     * @throws IOException if operations on inputStream fail.
     */
    @Throws(IOException::class)
    fun <T> deserialize(type: Type, inputStream: InputStream): T
}
