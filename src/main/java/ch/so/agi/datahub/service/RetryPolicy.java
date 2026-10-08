package ch.so.agi.datahub.service;

import java.time.LocalDateTime;

public final class RetryPolicy {
  private RetryPolicy() {}

  public static LocalDateTime next(int failures, LocalDateTime now) {
    return switch (failures) {
      case 1 -> now.plusMinutes(1);
      case 2 -> now.plusMinutes(5);
      case 3 -> now.plusMinutes(15);
      default -> null;
    };
  }
}
