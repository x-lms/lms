package com.example.lms.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class AttendanceHistory {
	private int historyNo;
	private int attNo;
	private int courseNo;
	private String stateBefore;
	private String stateAfter;
	private String historyComment;
	private String historyFile;
	private String historyFileOriginal;
	private MultipartFile newFile; 
	private String createdate;
}
