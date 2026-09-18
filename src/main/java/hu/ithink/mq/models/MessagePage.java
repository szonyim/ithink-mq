package hu.ithink.mq.models;

import java.util.List;

public record MessagePage(List<MessageRow> content, int page, int totalPages, long totalElements, int pageSize,
                           String query) {

  public boolean hasPrevious() {
    return page > 0;
  }

  public boolean hasNext() {
    return page + 1 < totalPages;
  }

  public boolean isEmpty() {
    return content.isEmpty();
  }
}
