package com.example.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.lms.dto.Student;
import com.example.lms.service.StudentService;

@Controller
public class StudentController {
    @Autowired
    private StudentService studentService;
    @GetMapping("/navStudent")
    public String navStudent(Model model) {
        Student student = studentService.getStudentById("");

        model.addAttribute("studentName", student.getStudentName()); 
        model.addAttribute("studnetImg", student.getStudnetImg());
        model.addAttribute("schedules", studentService.getStudentById(student.getStudentNo()));
        model.addAttribute("classes", studentService.getStudentById(student.getStudentNo()));

        return "navStudent"; 
    }
}



