package ch.so.agi.datahub.model;

public record JobPage(java.util.List<JobResponse> rows, int count, int page, int pageSize) {
  public boolean hasNext() {
    return (page + 1) * pageSize < count;
  }

  public boolean hasPrevious() {
    return page > 0;
  }
}
