package io.ebean;

import java.io.Serializable;

/**
 * Defines how a relationship is fetched via either normal SQL join,
 * a eager secondary query, via lazy loading or via eagerly hitting L2 cache.
 * <p>
 * <pre>{@code
 * // Normal fetch join results in a single SQL query
 * List<Order> list = DB.find(Order.class).fetch("details").findList();
 *
 * }</pre>
 * <p>
 * Example: Using a "query join" instead of a "fetch join" we instead use 2 SQL queries
 * </p>
 * <p>
 * <pre>{@code
 *
 * // This will use 2 SQL queries to build this object graph
 * List<Order> list =
 *     DB.find(Order.class)
 *         .fetch("details", FetchConfig.ofQuery())
 *         .findList();
 *
 * // query 1) find order
 * // query 2) find orderDetails where order.id in (?,?...) // first 100 order id's
 *
 * }</pre>
 *
 * @author mario
 * @author rbygrave
 */
public class FetchConfig implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int JOIN_MODE = 0;
  private static final int QUERY_MODE = 1;
  private static final int LAZY_MODE = 2;
  private static final int CACHE_MODE = 3;

  private int mode;
  private int batchSize;
  private int hashCode;

  /**
   * Construct using default JOIN mode.
   */
  public FetchConfig() {
    //this.mode = JOIN_MODE;
    this.batchSize = 100;
    this.hashCode = 1000;
  }

  private FetchConfig(int mode, int batchSize) {
    this.mode = mode;
    this.batchSize = batchSize;
    this.hashCode = mode + 10 * batchSize;
  }

  /**
   * Return FetchConfig that will eagerly fetch the relationship using L2 cache.
   * <p>
   * Any cache misses will be loaded by secondary query to the database.
   */
  public static FetchConfig ofCache() {
    return new FetchConfig(CACHE_MODE, 100);
  }

  /**
   * Return FetchConfig that use a eager secondary query to fetch the relationship.
   */
  public static FetchConfig ofQuery() {
    return new FetchConfig(QUERY_MODE, 100);
  }

  /**
   * Return FetchConfig that use a eager secondary query to fetch the relationship specifying the batch size.
   */
  public static FetchConfig ofQuery(int batchSize) {
    return new FetchConfig(QUERY_MODE, batchSize);
  }

  /**
   * Return FetchConfig that use lazy loading to fetch the relationship.
   */
  public static FetchConfig ofLazy() {
    return new FetchConfig(LAZY_MODE, 10);
  }

  /**
   * Return FetchConfig that use lazy loading to fetch the relationship specifying the batch size.
   */
  public static FetchConfig ofLazy(int batchSize) {
    return new FetchConfig(LAZY_MODE, batchSize);
  }

  /**
   * We want to migrate away from mutating FetchConfig to a fully immutable FetchConfig.
   */
  private FetchConfig mutate(int mode, int batchSize) {
    if (batchSize < 1) {
      throw new IllegalArgumentException("batch size "+batchSize+" must be > 0");
    }
    this.mode = mode;
    this.batchSize = batchSize;
    this.hashCode = mode + 10 * batchSize;
    return this;
  }

  /**
   * Specify that this path should be lazy loaded using the default batch load size.
   */
  public FetchConfig lazy() {
    return mutate(LAZY_MODE, 10);
  }

  /**
   * Specify that this path should be lazy loaded with a specified batch size.
   *
   * @param batchSize the batch size for lazy loading
   */
  public FetchConfig lazy(int batchSize) {
    return mutate(LAZY_MODE, batchSize);
  }

  /**
   * Eagerly fetccd h the beans in this path as a separate query (rather than as
   * part of the main query).
   * <p>
   * This will use the default batch size for separate query which is 100.
   * </p>
   */
  public FetchConfig query() {
    return mutate(QUERY_MODE, 100);
  }

  /**
   * Eagerly fetch the beans fetching the beans from the L2 bean cache
   * and using the DB for beans not in the cache.
   */
  public FetchConfig cache() {
    return mutate(CACHE_MODE, 100);
  }

  /**
   * Eagerly fetch the beans in this path as a separate query (rather than as
   * part of the main query).
   * <p>
   * The queryBatchSize is the number of parent id's that this separate query
   * will load per batch.
   * </p>
   * <p>
   * This will load all beans on this path eagerly unless a {@link #lazy(int)}
   * is also used.
   * </p>
   *
   * @param batchSize the batch size used to load beans on this path
   */
  public FetchConfig query(int batchSize) {
    return mutate(QUERY_MODE, batchSize);
  }

  /**
   * Eagerly fetch the first batch of beans on this path.
   * This is similar to {@link #query(int)} but only fetches the first batch.
   * <p>
   * If there are more parent beans than the batch size then they will not be
   * loaded eagerly but instead use lazy loading.
   * </p>
   *
   * @param batchSize the number of parent beans this path is populated for
   */
  @Deprecated
  public FetchConfig queryFirst(int batchSize) {
    return query(batchSize);
  }

  /**
   * Return the batch size for fetching.
   */
  public int getBatchSize() {
    return batchSize;
  }

  /**
   * Return true if the fetch should use the L2 cache.
   */
  public boolean isCache() {
    return mode == CACHE_MODE;
  }

  /**
   * Return true if the fetch should be a eager secondary query.
   */
  public boolean isQuery() {
    return mode == QUERY_MODE;
  }

  /**
   * Return true if the fetch should be a lazy query.
   */
  public boolean isLazy() {
    return mode == LAZY_MODE;
  }

  /**
   * Return true if the fetch should try to use SQL join.
   */
  public boolean isJoin() {
    return mode == JOIN_MODE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return (hashCode == ((FetchConfig) o).hashCode);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
