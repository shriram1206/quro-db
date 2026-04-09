package com.quro.quro.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryResponse {
    private String answer;
    private List<Map<String, Object>> rows;
    private String sqlUsed;
    private String source;
    private String question;
}