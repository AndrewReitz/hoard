package io.github.roguesdev.hoard;

/**
 * {@link Depositor}s store and retrieve values from the Pirate's Hoard!
 * All operations should be considered blocking IO operations.
 *
 * @param <T> The type the depositor saves and retrieves
 * @since 1.0
 */
public interface Depositor<T> {
  /**
   * Saves the value for future look up.
   * Passing in null is the same as deleting the value.
   *
   * @param value The value to store.
   */
  void store(T value);

  /**
   * Retrieve the store value if one exists.
   *
   * @return The saved value, otherwise null.
   */
  T retrieve();

  /**
   * Deletes the saved value.
   */
  void delete();

  /**
   * Checks if a value has been saved.
   *
   * @return true if a call to {@link #retrieve()} would return a value, false otherwise.
   */
  boolean exists();
}
