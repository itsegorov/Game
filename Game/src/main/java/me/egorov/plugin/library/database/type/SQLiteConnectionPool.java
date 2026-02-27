package me.egorov.plugin.library.database.type;

import com.zaxxer.hikari.HikariConfig;
import me.egorov.plugin.library.database.HikariConnectionPool;

public class SQLiteConnectionPool extends HikariConnectionPool {

    private SQLiteConnectionPool(HikariConfig config) {
        super(config);
    }

    public static class Factory extends AbstractPoolFactory<Factory> {

        public HikariConnectionPool build() {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setJdbcUrl(getUrl());
            hikariConfig.setMaximumPoolSize(maxPoolsSize);

            getProperties().forEach(hikariConfig::addDataSourceProperty);

            return new SQLiteConnectionPool(hikariConfig);
        }

        public String getUrl() {
            return "jdbc:sqlite:" + database;
        }

    }
}