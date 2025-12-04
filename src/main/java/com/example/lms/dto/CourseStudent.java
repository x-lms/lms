package com.example.lms.dto;

import lombok.Data;

@Data
// registration
public class CourseStudent {
	private int registNo;
	private int courseNo;
	private int studentNo;
	private int empNo;
	private String registState;
	private String courseName;
	private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String studentState;
    private String studentImg;
    
    private AttendanceSummary attendanceSummary; // 출결 요약
    
    private boolean alreadyChecked; // 출석체크 하루 한번 제한
     
}
