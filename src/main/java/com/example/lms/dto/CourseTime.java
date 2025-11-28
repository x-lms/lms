package com.example.lms.dto;

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
}
