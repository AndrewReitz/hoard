package io.github.roguesdev.hoard;

import io.github.roguesdev.hoard.serialization.Serializer;
import io.github.roguesdev.hoard.internal.ExceptionUtil;
import io.github.roguesdev.hoard.internal.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

class DefaultDepositor<T> implements Depositor<T> {

  private final Serializer serializer;
  private final File saveFile;
  private final Type type;

  DefaultDepositor(Serializer serializer, File directory, String key, Type type) {
    this.serializer = serializer;
    this.saveFile = new File(directory, key);
    this.type = type;
  }

  @Override public synchronized void store(T value) {
    if (value == null) {
      delete();
      return;
    }

    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(saveFile);
      serializer.serialize(type, value, fileOutputStream);
    } catch (IOException e) {
      ExceptionUtil.sneakyThrow(e);
    } finally {
      IOUtils.close(fileOutputStream);
    }
  }

  @Override public synchronized T retrieve() {
    if (!saveFile.exists()) {
      return null;
    }

    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(saveFile);
      return serializer.deserialize(type, fileInputStream);
    } catch (IOException e) {
      ExceptionUtil.sneakyThrow(e);
      return null;
    } finally {
      IOUtils.close(fileInputStream);
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override public synchronized void delete() {
    if (!saveFile.exists()) return;
    saveFile.delete();
  }
}
