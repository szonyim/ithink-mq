package hu.ithink.mq.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import hu.ithink.mq.models.MessagePage;
import hu.ithink.mq.models.MessageRow;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

// SQLite's trigram tokenizer can't match terms under 3 chars, so those fall back to LIKE.
@Service
public class MessageQueryService {

  public static final int DEFAULT_PAGE_SIZE = 25;
  private static final int MIN_PAGE_SIZE = 1;
  private static final int MAX_PAGE_SIZE = 500;
  private static final int CONTENT_PREVIEW_LENGTH = 300;
  private static final int MIN_TRIGRAM_TERM_LENGTH = 3;

  private final NamedParameterJdbcTemplate jdbc;

  public MessageQueryService(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public MessagePage search(String query, int page, int pageSize) {
    String term = query == null ? "" : query.trim();
    int requestedPage = Math.max(page, 0);
    int size = clampPageSize(pageSize);

    long totalElements = term.isEmpty() ? countAll() : countSearch(term);
    int totalPages = totalElements == 0 ? 0 : (int) Math.ceil(totalElements / (double) size);
    int safePage = totalPages == 0 ? 0 : Math.min(requestedPage, totalPages - 1);

    List<MessageRow> rows = term.isEmpty() ? findAllPage(safePage, size) : findSearchPage(term, safePage, size);
    return new MessagePage(rows, safePage, totalPages, totalElements, size, term);
  }

  private int clampPageSize(int pageSize) {
    if (pageSize <= 0) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(Math.max(pageSize, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
  }

  private long countAll() {
    Long count = jdbc.queryForObject("SELECT COUNT(*) FROM message", Map.of(), Long.class);
    return count == null ? 0 : count;
  }

  private List<MessageRow> findAllPage(int page, int pageSize) {
    Map<String, Object> params = Map.of("limit", pageSize, "offset", page * pageSize);
    return jdbc.query("""
        SELECT message_id, %s AS content_preview
        FROM message
        ORDER BY rowid DESC
        LIMIT :limit OFFSET :offset
        """.formatted(previewExpression()), params, this::mapRow);
  }

  private long countSearch(String term) {
    Map<String, Object> params = matchParams(term);
    String sql = isTrigramCapable(term)
        ? "SELECT COUNT(*) FROM message m WHERE m.rowid IN (SELECT rowid FROM message_fts WHERE message_fts MATCH :match)"
        : "SELECT COUNT(*) FROM message m WHERE " + likeWhereClause();
    Long count = jdbc.queryForObject(sql, params, Long.class);
    return count == null ? 0 : count;
  }

  private List<MessageRow> findSearchPage(String term, int page, int pageSize) {
    Map<String, Object> params = new java.util.HashMap<>(matchParams(term));
    params.put("limit", pageSize);
    params.put("offset", page * pageSize);

    String whereClause = isTrigramCapable(term)
        ? "m.rowid IN (SELECT rowid FROM message_fts WHERE message_fts MATCH :match)"
        : likeWhereClause();

    String sql = """
        SELECT m.message_id, %s AS content_preview
        FROM message m
        WHERE %s
        ORDER BY m.rowid DESC
        LIMIT :limit OFFSET :offset
        """.formatted(previewExpression("m.content"), whereClause);
    return jdbc.query(sql, params, this::mapRow);
  }

  private boolean isTrigramCapable(String term) {
    return term.length() >= MIN_TRIGRAM_TERM_LENGTH;
  }

  private Map<String, Object> matchParams(String term) {
    if (isTrigramCapable(term)) {
      return Map.of("match", ftsPhrase(term));
    }
    return Map.of("likePattern", "%" + escapeLike(term) + "%");
  }

  private String likeWhereClause() {
    return "m.message_id LIKE :likePattern ESCAPE '\\' "
        + "OR m.content LIKE :likePattern ESCAPE '\\' "
        + "OR m.properties LIKE :likePattern ESCAPE '\\'";
  }

  private String previewExpression() {
    return previewExpression("content");
  }

  private String previewExpression(String contentColumn) {
    return "CASE WHEN length(%s) > %d THEN substr(%s, 1, %d) || '…' ELSE %s END"
        .formatted(contentColumn, CONTENT_PREVIEW_LENGTH, contentColumn, CONTENT_PREVIEW_LENGTH, contentColumn);
  }

  private String ftsPhrase(String term) {
    return "\"" + term.replace("\"", "\"\"") + "\"";
  }

  private String escapeLike(String term) {
    return term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  private MessageRow mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new MessageRow(rs.getString("message_id"), rs.getString("content_preview"));
  }
}
