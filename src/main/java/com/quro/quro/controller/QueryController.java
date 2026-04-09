package com.quro.quro.controller;

import com.quro.quro.model.QueryRequest;
import com.quro.quro.model.QueryResponse;
import com.quro.quro.model.SchemaInfo;
import com.quro.quro.service.AIService;
import com.quro.quro.service.DatabaseService;
import com.quro.quro.service.RouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QueryController {

    @Autowired
    private RouterService routerService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private AIService aiService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/schema")
    public ResponseEntity<SchemaInfo> getSchema() {
        try {
            SchemaInfo schema = databaseService.fetchSchema();
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> processQuery(@RequestBody QueryRequest request) {
        try {
            String question = request.getQuestion();

            // Step 1: Route the question
            String route = routerService.classifyQuestion(question);

            if (!"database".equals(route)) {
                QueryResponse response = new QueryResponse(
                        "I can only answer questions about the college database (students, faculty, courses, etc.).",
                        Collections.emptyList(),
                        null,
                        "unsupported",
                        question
                );
                return ResponseEntity.ok(response);
            }

            // Step 2: Fetch schema
            SchemaInfo schema = databaseService.fetchSchema();

            // Step 3: Generate SQL
            String sql = aiService.generateSQL(question, schema);

            // Step 4: Execute query
            List<Map<String, Object>> rows = databaseService.executeQuery(sql);

            // Step 5: Generate natural language answer
            String answer = aiService.generateAnswer(question, rows, sql);

            QueryResponse response = new QueryResponse(
                    answer,
                    rows,
                    sql,
                    "database",
                    question
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            QueryResponse response = new QueryResponse(
                    "Invalid query: " + e.getMessage(),
                    Collections.emptyList(),
                    null,
                    "error",
                    request.getQuestion()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("Exception in processQuery: " + e.getMessage());
            e.printStackTrace();
            
            String userMessage = e.getMessage();
            if (userMessage != null && userMessage.contains("Rate limit exceeded")) {
                userMessage = "⏳ Rate limit reached. Please wait 2-3 minutes and try again.";
            } else if (userMessage != null && userMessage.contains("Invalid API key")) {
                userMessage = "🔑 API key error. Please check your configuration.";
            }
            
            QueryResponse response = new QueryResponse(
                    userMessage != null ? userMessage : "Error processing query: " + e.getMessage(),
                    Collections.emptyList(),
                    null,
                    "error",
                    request.getQuestion()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}