package net.cryptop.utils;

import java.util.function.Supplier;

import net.cryptop.App;

public class ChronoUtils {

  public static void time(String taskName, Runnable task) {
    long start = System.currentTimeMillis();
    task.run();
    long end = System.currentTimeMillis();
    App.getLogger().info(taskName + ": " + (end - start) + "ms");
  }

  public static <T> T time(String taskName, Supplier<T> task) {
    long start = System.currentTimeMillis();
    T result = task.get();
    long end = System.currentTimeMillis();
    App.getLogger().info(taskName + ": " + (end - start) + "ms");
    return result;
  }
}
