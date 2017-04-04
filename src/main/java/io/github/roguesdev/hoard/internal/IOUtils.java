package io.github.roguesdev.hoard.internal;

import java.io.Closeable;
import java.io.IOException;

public abstract class IOUtils {
  public static String UFT8 = "UTF-8";

  public static void close(Closeable... closeables) {
    for (Closeable closeable : closeables) {
      close(closeable);
    }
  }

  public static void close(Closeable closable) {
    if (closable == null) {
      return;
    }

    try {
      closable.close();
    } catch (IOException ignore) { }
  }
}
