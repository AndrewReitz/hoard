package io.github.roguesdev.hoard

/**
 * [Depositor]s store and retrieve values from the Pirate's Hoard!
 * All operations should be considered blocking IO operations.
 *
 * @param T The type the depositor saves and retrieves
 * @since 1.0
 */
interface Depositor<T> {
    /**
     * Saves the value for future look up.
     * Passing in null is the same as deleting the value.
     *
     * @param value The value to store.
     */
    fun store(value: T?)

    /**
     * Retrieve the store value if one exists.
     *
     * @return The saved value, otherwise null.
     */
    fun retrieve(): T?

    /**
     * Deletes the saved value.
     */
    fun delete()

    /**
     * Checks if a value has been saved.
     *
     * @return true if a call to [.retrieve] would return a value, false otherwise.
     */
    fun exists(): Boolean
}
