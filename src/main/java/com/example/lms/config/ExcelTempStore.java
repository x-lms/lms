package com.example.lms.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.lms.dto.StudentExcelResult;

@Component
public class ExcelTempStore {
    private final Map<String, StudentExcelResult> store = new ConcurrentHashMap<>();

    public void save(String token, StudentExcelResult result) {
        store.put(token, result);
    }

    public StudentExcelResult get(String token) {
        return store.get(token);
    }

    public void remove(String token) {
        store.remove(token);
    }
}
