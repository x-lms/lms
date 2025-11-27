package com.example.lms.dto;

import lombok.Data;

@Data
public class Assignment {
    private int projectNo;    
    private int courseNo;
    private String projectName;     // 프로젝트명
    private int empNo;   // 설명/내용
    private String projectDeadline;   // 마감일
}
