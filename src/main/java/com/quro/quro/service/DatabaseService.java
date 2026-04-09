package com.quro.quro.service;

import com.quro.quro.model.SchemaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
public class DatabaseService {

    @Autowired
    private DataSource dataSource;

    public SchemaInfo fetchSchema() throws SQLException {
        Map<String, List<SchemaInfo.ColumnInfo>> tables = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (!tableName.startsWith("sys_") && !tableName.equals("schema_version")) {
                        List<SchemaInfo.ColumnInfo> columns = new ArrayList<>();

                        try (ResultSet colRs = metaData.getColumns(conn.getCatalog(), null, tableName, "%")) {
                            while (colRs.next()) {
                                String colName = colRs.getString("COLUMN_NAME");
                                String colType = colRs.getString("TYPE_NAME");
                                columns.add(new SchemaInfo.ColumnInfo(colName, colType));
                            }
                        }

                        tables.put(tableName, columns);
                    }
                }
            }
        }

        return new SchemaInfo(tables);
    }

    public List<Map<String, Object>> executeQuery(String sql) throws SQLException {
        validateSQL(sql);

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.setMaxRows(100);

            try (ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
        }

        return results;
    }

    private void validateSQL(String sql) {
        String upper = sql.toUpperCase().trim();
        if (!upper.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed");
        }

        String[] forbidden = {"DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "CREATE", "TRUNCATE"};
        for (String keyword : forbidden) {
            if (upper.contains(keyword)) {
                throw new IllegalArgumentException("Query contains forbidden keyword: " + keyword);
            }
        }
    }
}