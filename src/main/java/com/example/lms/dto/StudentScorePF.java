package com.example.lms.dto;

import lombok.Data;

@Data
public class StudentScorePF {
	private int studentNo;
	private int courseNo;
	private String studentName;
	private Double scoreAtt;
	private Double scoreProject;
	private int scoreMid;
	private int scoreFin;
	private Double scoreTotal;
	private String scoreGrade;
	private String scoreStatus;
	
	// 이미 임시 등록된 학생 표시용
    private boolean alreadySubmitted;
    private int existsScore; // 0 = 점수 없음, 1 = 점수 있음	

	
}
