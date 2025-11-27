package com.example.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.lms.dto.Course;
import com.example.lms.dto.Student;
import com.example.lms.service.StudentAttendanceService;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student/attendanceList")
public class StudentAttendanceController {

    @Autowired
    private StudentAttendanceService attendanceService;

    // 1. 학생 출결 메인 화면
    @GetMapping("")
    public String attendanceHome(HttpSession session, Model model) {
        Student loginStudent = (Student) session.getAttribute("loginStudent");
        int studentNo = loginStudent.getStudentNo();

        // DB에서 최신 학생 정보 가져오기
        Student student = attendanceService.getStudentInfo(studentNo);

        // 세션에 최신 정보 갱신
        session.setAttribute("loginStudent", student);

        // 수강 강의 목록
        List<Course> courseList = attendanceService.getCourseList(studentNo);

        model.addAttribute("student", student);
        model.addAttribute("courseList", courseList);

        return "student/attendanceList";
    }


    // 2. 특정 강의 출결 상세 조회 
    @GetMapping("/detail")
    @ResponseBody
    public Map<String, Object> attendanceDetail(
            @RequestParam int courseNo,
            HttpSession session
    ) {
        Student loginStudent = (Student) session.getAttribute("loginStudent");

        // 최신 학생 정보로 DB 조회
        int studentNo = loginStudent.getStudentNo();
        return attendanceService.getAttendanceDetail(studentNo, courseNo);
    }
}
