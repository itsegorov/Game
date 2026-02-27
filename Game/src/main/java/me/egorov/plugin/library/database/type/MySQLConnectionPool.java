package me.egorov.plugin.library.database.type;

import com.zaxxer.hikari.HikariConfig;
import me.egorov.plugin.library.database.HikariConnectionPool;
import org.jetbrains.annotations.NotNull;

public class MySQLConnectionPool extends HikariConnectionPool {

    private MySQLConnectionPool(HikariConfig hikariConfig) {
        super(hikariConfig);
    }

    public static class Factory extends AbstractPoolFactory<Factory> {

        private String hostname = "localhost";
        private String port = "3306";
        private String username = "root";
        private String password;

        public Factory hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Factory port(String port) {
            this.port = port;
            return this;
        }

        public Factory username(String username) {
            this.username = username;
            return this;
        }

        public Factory password(String password) {
            this.password = password;
            return this;
        }

        public @NotNull Factory maxPoolsSize(int size) {
            this.maxPoolsSize = size;
            return this;
        }

        public HikariConnectionPool build() {
            HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setJdbcUrl(getUrl());
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(maxPoolsSize);
            hikariConfig.setMaxLifetime(864000);

            getProperties().forEach(hikariConfig::addDataSourceProperty);

            return new MySQLConnectionPool(hikariConfig);
        }

        public String getUrl() {
            return "jdbc:mysql://" + hostname + ":" + port + "/"
                    + database + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&wait_timeout=864000";
        }

    }
}