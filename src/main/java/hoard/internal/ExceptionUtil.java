package hoard.internal;

public abstract class ExceptionUtil {

  /**
   * Allows throwing of a checked exception without declaring it in a throws clause..
   *
   * @param t throwable to throw as a runtime exception.
   */
  public static void sneakyThrow(Throwable t) {
    ExceptionUtil.<RuntimeException>doSneakyThrow(t);
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  private static <T extends Throwable> void doSneakyThrow(Throwable t) throws T {
    throw (T) t;
  }
}
