package com.example.lms.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TimetableCell {
	private int period; // 1~9교시
    private List<String> mon = new ArrayList<>();
    private List<String> tue = new ArrayList<>();
    private List<String> wed = new ArrayList<>();
    private List<String> thu = new ArrayList<>();
    private List<String> fri = new ArrayList<>();
}
