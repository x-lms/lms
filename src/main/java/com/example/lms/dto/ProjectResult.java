package com.example.lms.dto;

import lombok.Data;

@Data
public class ProjectResult {
	private Integer resultNo;
	private Integer projectNo;
	private Integer studentNo;
	private String resultTitle;
	private String resultContents;
	private String resultFile;
	private String createdate;
	private Integer resultScore;
	private String studentName; // 추가
	
}
