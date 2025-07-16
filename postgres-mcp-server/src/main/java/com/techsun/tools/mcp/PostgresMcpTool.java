package com.techsun.tools.mcp;

import java.sql.Connection;
import java.sql.SQLException;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;

@McpServerEndpoint(channel = McpChannel.STDIO)
public class PostgresMcpTool {
    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return PostgresDataSourceManager.getConnection();
    }

    /**
     * 执行SQL查询（仅支持SELECT）
     */
    @ToolMapping(description = "执行SQL查询（仅支持SELECT）")
    public String execute_query(@Param(name = "sql", description = "要执行的SELECT语句") String sql) {
        if (sql == null || !sql.trim().toLowerCase().startsWith("select")) {
            return "只允许执行SELECT语句";
        }
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
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
        } catch (Exception e) {
            return "SQL执行异常: " + e.getMessage();
        }
    }

    /**
     * 列出指定 schema 下所有表
     */
    @ToolMapping(description = "列出指定 schema 下所有表")
    public String list_tables(@Param(name = "schema", description = "schema 名称", required = false) String schema) {
        String sql;
        if (schema == null || schema.isEmpty()) {
            sql = "SELECT schemaname, tablename FROM pg_catalog.pg_tables WHERE schemaname NOT IN ('pg_catalog', 'information_schema') ORDER BY schemaname, tablename";
        } else {
            sql = "SELECT schemaname, tablename FROM pg_catalog.pg_tables WHERE schemaname = ? ORDER BY tablename";
        }
        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (schema != null && !schema.isEmpty()) {
                stmt.setString(1, schema);
            }
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("schemaname\ttablename\n");
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("\t").append(rs.getString(2)).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "获取表列表异常: " + e.getMessage();
        }
    }

    /**
     * 获取指定 schema 下指定表结构
     */
    @ToolMapping(description = "获取指定 schema 下指定表结构")
    public String get_table_schema(@Param(name = "table", description = "表名") String table,
                                   @Param(name = "schema", description = "schema 名称", required = false) String schema) {
        String sql = "SELECT column_name, data_type, is_nullable, column_default FROM information_schema.columns WHERE table_name = ?";
        if (schema != null && !schema.isEmpty()) {
            sql += " AND table_schema = ?";
        }
        sql += " ORDER BY ordinal_position";
        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, table);
            if (schema != null && !schema.isEmpty()) {
                stmt.setString(2, schema);
            }
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("column_name\tdata_type\tis_nullable\tcolumn_default\n");
                while (rs.next()) {
                    sb.append(rs.getString(1)).append("\t")
                      .append(rs.getString(2)).append("\t")
                      .append(rs.getString(3)).append("\t")
                      .append(rs.getString(4)).append("\n");
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
        // 本地仅返回当前配置实例信息，可扩展为多实例管理
        String url = org.noear.solon.Solon.cfg().get("postgres.datasource.url");
        String user = org.noear.solon.Solon.cfg().get("postgres.datasource.username");
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
        String sql = "SELECT datname, numbackends, xact_commit, xact_rollback, blks_read, blks_hit FROM pg_stat_database WHERE datname = current_database()";
        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            StringBuilder sb = new StringBuilder();
            sb.append("datname\tnumbackends\txact_commit\txact_rollback\tblks_read\tblks_hit\n");
            while (rs.next()) {
                for (int i = 1; i <= 6; i++) {
                    sb.append(rs.getString(i));
                    if (i < 6) sb.append("\t");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "获取数据库统计信息异常: " + e.getMessage();
        }
    }
}
