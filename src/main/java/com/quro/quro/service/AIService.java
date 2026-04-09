package com.quro.quro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quro.quro.model.SchemaInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Value("${demo.mode:false}")
    private boolean demoMode;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateSQL(String question, SchemaInfo schema) {
        if (demoMode) {
            return generateDemoSQL(question);
        }
        StringBuilder schemaStr = new StringBuilder();
        for (Map.Entry<String, List<SchemaInfo.ColumnInfo>> entry : schema.getTables().entrySet()) {
            schemaStr.append("Table: ").append(entry.getKey()).append("\nColumns:\n");
            for (SchemaInfo.ColumnInfo col : entry.getValue()) {
                schemaStr.append("  - ").append(col.getName()).append(" (").append(col.getType()).append(")\n");
            }
            schemaStr.append("\n");
        }

        String systemPrompt = """
            You are a MySQL SQL expert. Generate ONLY the SQL query, nothing else.
            
            CRITICAL ANTI-DUPLICATE RULES:
            1. ALWAYS use SELECT DISTINCT when querying student/faculty/staff entities
            2. When JOINing tables, ALWAYS use DISTINCT to prevent duplicate rows
            3. For queries like "students who...", "faculty who...", use: SELECT DISTINCT s.* FROM students s JOIN...
            4. Never return duplicate rows - verify your JOIN logic
            5. If counting, use COUNT(DISTINCT entity_id)
            6. For multi-table queries, always add DISTINCT after SELECT
            7. Check foreign key relationships - use proper JOIN conditions
            8. enrollment table links students to courses - expect multiple rows per student
            9. attendance table has multiple records per student - use DISTINCT when listing students
            10. ALWAYS prefer DISTINCT over GROUP BY for entity listings
            
            LIMIT RULES:
            1. If user asks for "top 5", "top 10", etc., use ORDER BY and LIMIT with that exact number
            2. If user asks for "first 3", "show 5", use LIMIT with that number
            3. For "top students", order by cgpa DESC or grade DESC and apply limit
            4. For "bottom/worst", order by cgpa ASC or grade ASC
            5. If no specific number mentioned, limit to 100 rows maximum
            
            Rules:
            - Return ONLY the SQL query, no explanations
            - Use proper JOIN syntax
            - Respect user's requested limits (top 5 means LIMIT 5, not LIMIT 100)
            - Use table and column names exactly as provided
            - Handle NULL values appropriately
            """;

        String userPrompt = "Database Schema:\n" + schemaStr + "\n\nQuestion: " + question + "\n\nSQL Query:";

        try {
            String response = callOpenAI(systemPrompt, userPrompt, 0.0, 500);
            // Remove markdown code blocks if present
            response = response.replaceAll("```sql\\n?", "").replaceAll("```\\n?", "").trim();
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SQL: " + e.getMessage(), e);
        }
    }

    public String generateAnswer(String question, List<Map<String, Object>> rows, String sql) {
        if (demoMode) {
            return generateDemoAnswer(question, rows);
        }

        String systemPrompt = """
            You are a helpful assistant that explains database query results in plain English.
            Be concise and natural. Don't mention SQL or technical details.
            """;

        String userPrompt = String.format(
                "Question: %s\n\nQuery Results (%d rows):\n%s\n\nProvide a clear, natural answer:",
                question,
                rows.size(),
                formatRowsForPrompt(rows)
        );

        try {
            return callOpenAI(systemPrompt, userPrompt, 0.3, 300);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate answer: " + e.getMessage(), e);
        }
    }

    private String callOpenAI(String systemPrompt, String userPrompt, double temperature, int maxTokens) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);

        requestBody.set("messages", messages);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "/chat/completions",
                    request,
                    String.class
            );

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("429")) {
                throw new RuntimeException("Rate limit exceeded. Please wait a few minutes and try again.", e);
            } else if (errorMsg != null && errorMsg.contains("401")) {
                throw new RuntimeException("Invalid API key. Please check your GitHub token.", e);
            } else {
                throw new RuntimeException("API call failed: " + errorMsg, e);
            }
        }
    }

    private String formatRowsForPrompt(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "No results found";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map<String, Object> row : rows) {
            if (count++ >= 10) {
                sb.append("... and ").append(rows.size() - 10).append(" more rows");
                break;
            }
            sb.append(row.toString()).append("\n");
        }
        return sb.toString();
    }

    // Demo mode helpers
    private String generateDemoSQL(String question) {
        String lower = question.toLowerCase();
        
        // Extract limit from question (top 5, top 10, etc.)
        int limit = 100;
        if (lower.contains("top")) {
            // Try to extract number after "top"
            String[] words = lower.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].equals("top")) {
                    try {
                        limit = Integer.parseInt(words[i + 1].replaceAll("[^0-9]", ""));
                        break;
                    } catch (NumberFormatException e) {
                        limit = 5; // default for "top" without number
                    }
                }
            }
        } else if (lower.matches(".*\\b(\\d+)\\s+(student|faculty|course).*")) {
            // Extract number like "show 5 students"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(\\d+)\\s+");
            java.util.regex.Matcher matcher = pattern.matcher(lower);
            if (matcher.find()) {
                try {
                    limit = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    limit = 100;
                }
            }
        }
        
        // Generate SQL based on question type
        if (lower.contains("top") && (lower.contains("student") || lower.contains("cgpa") || lower.contains("grade"))) {
            return "SELECT DISTINCT * FROM students ORDER BY cgpa DESC LIMIT " + limit;
        } else if (lower.contains("all students") || lower.contains("show students")) {
            return "SELECT DISTINCT * FROM students LIMIT " + limit;
        } else if (lower.contains("computer science") && lower.contains("student")) {
            return "SELECT DISTINCT * FROM students WHERE department = 'Computer Science' LIMIT " + limit;
        } else if (lower.contains("faculty") && lower.contains("teach")) {
            return "SELECT DISTINCT f.* FROM faculty f JOIN courses c ON f.id = c.faculty_id WHERE c.course_name LIKE '%Database%' LIMIT " + limit;
        } else if (lower.contains("all faculty") || lower.contains("show faculty")) {
            return "SELECT DISTINCT * FROM faculty LIMIT " + limit;
        } else if (lower.contains("all courses") || lower.contains("show courses")) {
            return "SELECT * FROM courses LIMIT " + limit;
        } else if (lower.contains("exam schedule") || lower.contains("exam")) {
            return "SELECT * FROM exam_schedule WHERE department = 'Computer Science' LIMIT " + limit;
        } else if (lower.contains("hostel") || lower.contains("room")) {
            return "SELECT * FROM hostel_rooms LIMIT " + limit;
        } else if (lower.contains("above") || lower.contains("score") || lower.contains("90")) {
            return "SELECT DISTINCT s.* FROM students s WHERE s.cgpa >= 9.0 LIMIT " + limit;
        } else {
            return "SELECT DISTINCT * FROM students LIMIT " + limit;
        }
    }

    private String generateDemoAnswer(String question, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "No results found in the database for this query.";
        }
        
        String lower = question.toLowerCase();
        if (lower.contains("top") && rows.size() > 1) {
            return String.format("Here are the top %d student%s based on CGPA. The highest CGPA shown is from the best performing students.", 
                rows.size(), 
                rows.size() == 1 ? "" : "s");
        }
        
        return String.format("Found %d result%s. The data includes information from the college database.", 
            rows.size(), 
            rows.size() == 1 ? "" : "s");
    }
}