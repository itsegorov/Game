package me.egorov.plugin.library.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class HikariConnectionPool implements AbstractConnectionPool {

    public static final String CHECK_QUERY = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";

    private final HikariDataSource hikariDataSource;

    protected HikariConnectionPool(HikariConfig config) {
        this.hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public @NotNull Connection connection() throws SQLException {
        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLException("Failed to get connection from pool", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    @Override
    public HikariDataSource getDataSource() {
        return hikariDataSource;
    }

    @Override
    public void close() {
        hikariDataSource.close();
    }

    public abstract static class AbstractPoolFactory<T extends AbstractPoolFactory<T>> {

        protected final Map<String, Object> property = new HashMap<>();
        protected int maxPoolsSize = 1;
        protected String name;
        protected String database;

        public T addProperty(String value, Object data) {
            property.put(value, data);
            return (T) this;
        }

        public T database(String database) {
            this.database = database;
            return (T) this;
        }

        public T setName(String value) {
            this.name = value;
            return (T) this;
        }

        public T setMaxPoolsSize(int maxPoolsSize) {
            this.maxPoolsSize = maxPoolsSize;
            return (T) this;
        }

        public Map<String, Object> getProperties() {
            return property;
        }

        public abstract AbstractConnectionPool build();

    }
}
