package com.example.lms.dto;

import lombok.Data;

@Data
public class Project {
	private int projectNo;
	private int courseNo;
	private String projectName;
	private int empNo;
	private String projectDeadline;
	private String courseName; // 추가
	
	
}
