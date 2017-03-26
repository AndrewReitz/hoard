package hoard;

/**
 *
 * @param <T> The type the depositor saves and retrieves
 * @since 1.0
 */
public interface Depositor<T> {
  /**
   * Saves the value for future look up.
   *
   * @param value The value to save.
   */
  void save(T value);

  /**
   * Retrieve the save value if one exists.
   *
   * @return The saved value, otherwise null.
   */
  T retrieve();

  /**
   * Deletes the saved value.
   */
  void delete();
}
