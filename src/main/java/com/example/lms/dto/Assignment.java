package com.example.lms.dto;

import lombok.Data;

@Data
public class Assignment {

    private int projectNo;    
    private int courseNo;
    private String projectName;     
    private int empNo;
    private String projectDeadline;   // yyyy-MM-dd
    private boolean closed;   // 마감 여부
}
