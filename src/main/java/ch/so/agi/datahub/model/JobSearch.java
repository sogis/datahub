package ch.so.agi.datahub.model;

public record JobSearch(
    String organisation,
    String theme,
    String operat,
    String status,
    String validationStatus,
    int page,
    String sort) {
  public JobSearch {
    page = Math.max(0, page);
    if (sort == null) sort = "date-desc";
  }

  public static JobSearch all() {
    return new JobSearch(null, null, null, null, null, 0, "date-desc");
  }
}
