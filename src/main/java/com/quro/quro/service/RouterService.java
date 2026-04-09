package com.quro.quro.service;

import org.springframework.stereotype.Service;

@Service
public class RouterService {

    public String classifyQuestion(String question) {
        String lower = question.toLowerCase();

        String[] dbKeywords = {"student", "faculty", "course", "enrollment", "staff",
                "attendance", "exam", "library", "hostel", "show", "list",
                "find", "get", "which", "who", "what", "how many", "count"};

        for (String keyword : dbKeywords) {
            if (lower.contains(keyword)) {
                return "database";
            }
        }

        return "unsupported";
    }
}