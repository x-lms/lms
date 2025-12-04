package com.example.lms.dto;

import lombok.Data;

@Data
public class Score {
	private int scoreNo; //점수번호
	private int studentNo; // 학생번호
	private String studentName; // 학생이름(추가)
	private int empNo;     //교수번호
	private int courseNo;  //과목이름
	private double scoreAtt; //출석점수 출석은 몇센티지로 계산할거임 지각3회는 결석1회로 산정하고 결석이 4회이상이면 중간 기말 과제 점수랑 상관없이 무조건 f 처리
	private double scoreProject;//과제점수
	private int scoreMid; // 중간고사 
	private int scoreFin; //기말고사 
	private double scoreTotal; //총점계산 100점만점 중간:30점만점 기말:33점만점 과제점수:20점만점 출결:20점만점
	private String scoreGrade; // 상위 30프로 A, 순차적으로 B,C,D 
	private String scoreStatus; // 임시 'T' 최종'F'
	private String courseName; // 과목명(추가)
	

}

