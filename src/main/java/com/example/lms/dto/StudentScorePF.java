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
	
}
