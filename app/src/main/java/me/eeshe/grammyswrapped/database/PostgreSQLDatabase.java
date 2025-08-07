package me.eeshe.grammyswrapped.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.eeshe.grammyswrapped.util.AppConfig;

public class PostgreSQLDatabase {
  private static PostgreSQLDatabase instance;
  private HikariDataSource dataSource;

  // Private constructor to enforce the singleton pattern
  private PostgreSQLDatabase() {
    HikariConfig config = new HikariConfig();
    AppConfig appConfig = new AppConfig();

    // --- PostgreSQL Specific Configuration ---
    // JDBC URL for PostgreSQL: jdbc:postgresql://<host>:<port>/<database>
    String host = appConfig.getPostgreSQLHost();
    String port = appConfig.getPostgreSQLPort();
    String database = appConfig.getPostgreSQLDatabase();

    config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
    config.setUsername(appConfig.getPostgreSQLUsername());
    config.setPassword(appConfig.getPostgreSQLPassword());

    // Optional: Specify the driver class name explicitly (HikariCP usually infers
    // it)
    // config.setDriverClassName("org.postgresql.Driver");

    // --- HikariCP Pool Configuration ---
    config.addDataSourceProperty("cachePrepStmts", "true"); // Optimize prepared statements
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    // Add more DataSource properties as needed, e.g., for SSL:
    // config.addDataSourceProperty("sslmode", "require");

    try {
      dataSource = new HikariDataSource(config);
      System.out.println("HikariCP PostgreSQL connection pool initialized successfully.");
    } catch (Exception e) {
      System.err.println("Error initializing HikariCP PostgreSQL connection pool: " + e.getMessage());
      e.printStackTrace();
      // Handle initialization failure appropriately, e.g., throw a runtime exception
      throw new RuntimeException("Failed to initialize database connection pool.", e);
    }
  }

  /**
   * Gets the singleton instance of the PostgreSQLConnection manager.
   *
   * @return The singleton instance.
   */
  public static synchronized PostgreSQLDatabase getInstance() {
    if (instance == null) {
      instance = new PostgreSQLDatabase();
    }
    return instance;
  }

  /**
   * Retrieves a database connection from the HikariCP pool.
   * Remember to close the connection in a try-with-resources block after use.
   *
   * @return A java.sql.Connection object.
   * @throws SQLException If a database access error occurs.
   */
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  /**
   * Shuts down the HikariCP connection pool, releasing all resources.
   * This method should be called when your application is shutting down.
   */
  public void shutdown() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      System.out.println("HikariCP PostgreSQL connection pool shut down.");
    }
  }
}
