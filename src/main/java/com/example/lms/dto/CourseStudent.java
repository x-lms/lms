package com.example.lms.dto;

import lombok.Data;

@Data
public class CourseStudent {
	private int courseNo;
	private int studentNo;
	private String courseName;
	private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String studentState;
    private String studentImg;
    
    private AttendanceSummary attendanceSummary;
}
