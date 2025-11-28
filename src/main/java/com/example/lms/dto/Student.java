package com.example.lms.dto;

import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class Student {
private int studentNo;
private int deptNo;
private String deptName;   // 추가
private String studentPw;
private String studentName;
private String studentState; // 상태 (재학/휴학 등)
private String studentAddress;
private String studentPhone;
private String studentBirth;
private String studentImg;
private MultipartFile studentImgFile;
private String studentEmail;
}
