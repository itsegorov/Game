package me.egorov.plugin.library.database;

import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

public interface AbstractConnectionPool extends Closeable {

    Connection getConnection() throws SQLException;

    HikariDataSource getDataSource();

    @NotNull Connection connection() throws SQLException;
}
