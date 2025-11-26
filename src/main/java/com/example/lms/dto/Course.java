package com.example.lms.dto;

import lombok.Data;

@Data
public class Course {
	private int courseNo;
	private String courseName;
	private int deptNo;
	private int empNo;
	private int maxCnt;
	private int currentCnt;
	private String coursePeriod;
	private String coursePlan;
	
}