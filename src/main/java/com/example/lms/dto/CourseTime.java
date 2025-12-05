package com.example.lms.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CourseTime {
	private int courseTimeNo;
	private int courseNo;
	private String courseName; 
	private String courseLocation;
	private String coursedate; // 요일
	private String courseTimeStart;
	private String courseTimeEnd;
	
	// Mustache 출력용(요일)
	private String dayOptions;
	private int index;

	 // 강의 기간 필드 추가
    private LocalDate courseStartDate; // 강의 시작일
    private LocalDate courseEndDate;   // 강의 종료일	
	
}
