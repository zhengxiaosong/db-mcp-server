package com.techsun.tools.mcp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.Solon;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接池管理类
 */
public class PostgresDataSourceManager {
    private static final HikariDataSource dataSource;
    static {
        Properties props = Solon.cfg().getProp("postgres.datasource.");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("url"));
        config.setUsername(props.getProperty("username"));
        config.setPassword(props.getProperty("password"));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("minimumIdle", "2")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("idleTimeout", "60000")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("connectionTimeout", "30000")));
        config.setPoolName(props.getProperty("poolName", "PostgresMcpHikariCP"));
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}

