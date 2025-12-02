package com.example.lms.dto;

import lombok.Data;
@Data
public class Question {
    private int questionNo;
    private int studentNo;
    private int empNo;
    private int courseNo;
    private String questionTitle;
    private String questionContents;
    private String answerStatus; //  응답/미응답
    private String answerContents; // 교수 답변 내용
    private String createdate;
    private String courseName;
    private boolean answered; 

    public boolean getIsAnswered() {
        return "Y".equalsIgnoreCase(answerStatus);
    }

	
}



