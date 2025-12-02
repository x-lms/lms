package com.example.lms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.example.lms.dto.Student;
import com.example.lms.dto.Score;
import com.example.lms.service.StudentScoreService;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentGradeController {

    private final StudentScoreService studentScoreService;

    // 성적 리스트 조회
    @GetMapping("student/gradeList")
    public String gradeList(@SessionAttribute("loginStudent") Student student, Model model) {

      
        List<Score> gradeList = studentScoreService.getScoreList(student.getStudentNo());

        model.addAttribute("gradeList", gradeList);
        return "student/gradeList"; 
    }

}
