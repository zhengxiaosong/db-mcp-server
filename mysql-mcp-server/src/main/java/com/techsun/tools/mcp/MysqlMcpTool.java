package com.techsun.tools.mcp;

import java.sql.Connection;
import java.sql.SQLException;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;

@McpServerEndpoint(channel = McpChannel.STDIO)
public class MysqlMcpTool {
    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return MySQLDataSourceManager.getConnection();
    }

    /**
     * 执行SQL查询（仅支持SELECT），支持动态切换数据库
     */
    @ToolMapping(description = "执行SQL查询（仅支持SELECT），支持动态切换数据库")
    public String execute_query(@Param(name = "sql", description = "要执行的SELECT语句") String sql,
                                @Param(name = "database", description = "目标数据库名", required = false) String database) {
        if (sql == null || !sql.trim().toLowerCase().startsWith("select")) {
            return "只允许执行SELECT语句";
        }
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            if (database != null && !database.isEmpty()) {
                stmt.execute("USE `" + database + "`");
            }
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                StringBuilder sb = new StringBuilder();
                // 输出表头
                for (int i = 1; i <= colCount; i++) {
                    sb.append(meta.getColumnLabel(i));
                    if (i < colCount) sb.append("\t");
                }
                sb.append("\n");
                // 输出数据
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        sb.append(rs.getString(i));
                        if (i < colCount) sb.append("\t");
                    }
                    sb.append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "SQL执行异常: " + e.getMessage();
        }
    }

    /**
     * 列出所有表，支持动态切换数据库
     */
    @ToolMapping(description = "列出所有表，支持动态切换数据库")
    public String list_tables(@Param(name = "database", description = "目标数据库名", required = false) String database) {
        String sql = "SHOW TABLES";
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            if (database != null && !database.isEmpty()) {
                stmt.execute("USE `" + database + "`");
            }
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "获取表列表异常: " + e.getMessage();
        }
    }

    /**
     * 获取指定表结构，支持动态切换数据库
     */
    @ToolMapping(description = "获取指定表结构，支持动态切换数据库")
    public String get_table_schema(@Param(name = "table", description = "表名") String table,
                                   @Param(name = "database", description = "目标数据库名", required = false) String database) {
        String sql = "SHOW COLUMNS FROM `" + table + "`";
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            if (database != null && !database.isEmpty()) {
                stmt.execute("USE `" + database + "`");
            }
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Field\tType\tNull\tKey\tDefault\tExtra\n");
                while (rs.next()) {
                    for (int i = 1; i <= 6; i++) {
                        sb.append(rs.getString(i));
                        if (i < 6) sb.append("\t");
                    }
                    sb.append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "获取表结构异常: " + e.getMessage();
        }
    }

    /**
     * 筛选数据库实例（本地仅返回当前配置实例信息）
     */
    @ToolMapping(description = "筛选数据库实例（本地仅返回当前配置实例信息）")
    public String filter_instances(@Param(name = "name", description = "实例名，支持模糊匹配", required = false) String name) {
        String url = org.noear.solon.Solon.cfg().get("mysql.datasource.url");
        String user = org.noear.solon.Solon.cfg().get("mysql.datasource.username");
        if (name == null || url.contains(name) || user.contains(name)) {
            return String.format("name: %s\nurl: %s\nuser: %s\n", "default", url, user);
        } else {
            return "无匹配实例";
        }
    }

    /**
     * 获取数据库统计信息
     */
    @ToolMapping(description = "获取数据库统计信息")
    public String get_database_stats() {
        String sql = "SHOW STATUS WHERE Variable_name IN ('Threads_connected','Questions','Uptime','Slow_queries','Connections')";
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Variable_name\tValue\n");
            while (rs.next()) {
                sb.append(rs.getString(1)).append("\t").append(rs.getString(2)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "获取数据库统计信息异常: " + e.getMessage();
        }
    }

    /**
     * 列出当前用户有权限访问的所有数据库
     */
    @ToolMapping(description = "列出当前用户有权限访问的所有数据库")
    public String list_databases() {
        String sql = "SHOW DATABASES";
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString(1)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "获取数据库列表异常: " + e.getMessage();
        }
    }
}
