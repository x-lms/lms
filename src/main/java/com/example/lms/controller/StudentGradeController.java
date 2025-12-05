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

    @GetMapping("student/gradeList")
    public String gradeList(@SessionAttribute("loginStudent") Student student, Model model) {

        List<Score> allGrades = studentScoreService.getScoreList(student.getStudentNo());

        // F 일때만 성적이 보일수 있음
        List<Score> finalGrades = allGrades.stream()
                                           .filter(score -> "F".equals(score.getScoreStatus()))
                                           .toList();

        model.addAttribute("gradeList", finalGrades);
        return "student/gradeList"; 
    }

}
