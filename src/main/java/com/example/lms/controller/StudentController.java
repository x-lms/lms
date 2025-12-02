package com.example.lms.controller;

import com.example.lms.dto.Student;
import com.example.lms.dto.TimetableCell;
import com.example.lms.service.StudentService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

private final StudentService studentService;
private final String uploadDir = "C:/lms/uploads";
private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

@PostConstruct
public void init() {
    File dir = new File(uploadDir);
    if (!dir.exists()) {
        dir.mkdirs();
        log.info("Upload directory created: {}", uploadDir);
    }
}

// 학생 홈
@GetMapping("/studentHome")
public String studentHome(
        @SessionAttribute("loginStudent") Student loginStudent,
        Model model) {

    int studentNo = loginStudent.getStudentNo();

    // 학생 시간표 가져오기
    List<TimetableCell> timetable = studentService.getStudentSchedule(studentNo);
    model.addAttribute("timetable", timetable);

    return "student/studentHome"; // 머스테치 파일
}


// 학생 본인 정보 조회
@GetMapping("/studentInfo")
public String studentInfo(HttpSession session, Model model) {
    Student loginStudent = getLoginStudent(session);
    Student s = studentService.getStudentDetail(loginStudent.getStudentNo());
    normalizeStudentFields(s);
    model.addAttribute("s", s);
    return "student/studentInfo";
}

// 학생 정보 수정 페이지
@GetMapping("/modifyStudentInfo")
public String modifyStudentInfo(HttpSession session, Model model) {
    Student loginStudent = getLoginStudent(session);
    Student s = studentService.getStudentDetail(loginStudent.getStudentNo());
    normalizeStudentFields(s);
    model.addAttribute("s", s);
    return "student/modifyStudentInfo";
}

// 학생 정보 수정 처리
@PostMapping("/modifyStudentInfo")
public String modifyStudentInfo(
        @ModelAttribute Student s,
        @RequestParam("studentImgFile") MultipartFile studentImgFile,
        HttpSession session
) {
    Student loginStudent = getLoginStudent(session);
    int studentNo = loginStudent.getStudentNo();
    Student old = studentService.getStudentDetail(studentNo);
    s.setStudentNo(studentNo);

    if (studentImgFile != null && !studentImgFile.isEmpty()) {
        String originalName = studentImgFile.getOriginalFilename();
        String ext = getExtension(originalName);

        if (!allowedExtensions.contains(ext.toLowerCase())) {
            log.warn("허용되지 않은 파일 형식: {}", originalName);
            s.setStudentImg(old.getStudentImg());
        } else {
            String fileName = UUID.randomUUID() + "_" + originalName;
            File dest = new File(uploadDir, fileName);
            dest.getParentFile().mkdirs();
            try {
                studentImgFile.transferTo(dest);
                s.setStudentImg("/uploads/" + fileName);
            } catch (IOException e) {
                log.error("파일 업로드 실패", e);
                s.setStudentImg(old.getStudentImg());
            }
        }
    } else {
        s.setStudentImg(old.getStudentImg());
    }

    if (isEmpty(s.getStudentState())) s.setStudentState(old.getStudentState());
    if (isEmpty(s.getStudentBirth())) s.setStudentBirth(old.getStudentBirth());
    if (isEmpty(s.getStudentPhone())) s.setStudentPhone(old.getStudentPhone());
    if (isEmpty(s.getStudentAddress())) s.setStudentAddress(old.getStudentAddress());
    if (isEmpty(s.getStudentEmail())) s.setStudentEmail(old.getStudentEmail());
    if (isEmpty(s.getStudentPw())) s.setStudentPw(old.getStudentPw());
    if (s.getDeptNo() == 0) s.setDeptNo(old.getDeptNo());

    studentService.updateStudent(s);
    return "redirect:/student/studentInfo";
}

// ----------------------------
// 공통 유틸
// ----------------------------
private Student getLoginStudent(HttpSession session) {
    Student loginStudent = (Student) session.getAttribute("loginStudent");
    if (loginStudent == null) throw new RuntimeException("로그인 세션 없음");
    return loginStudent;
}

private void normalizeStudentFields(Student s) {
    if (s.getStudentBirth() == null) s.setStudentBirth("");
    if (s.getStudentPhone() == null) s.setStudentPhone("");
    if (s.getStudentImg() == null) s.setStudentImg("");
    if (s.getStudentState() == null) s.setStudentState("");
    if (s.getStudentAddress() == null) s.setStudentAddress("");
    if (s.getStudentEmail() == null) s.setStudentEmail("");
    if (s.getDeptNo() == 0) s.setDeptNo(0);
}

private boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
}

private String getExtension(String filename) {
    if (filename == null) return "";
    int idx = filename.lastIndexOf('.');
    return (idx != -1) ? filename.substring(idx + 1) : "";
}


}
