package com.quro.quro.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaInfo {
    private Map<String, List<ColumnInfo>> tables;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColumnInfo {
        private String name;
        private String type;
    }
}