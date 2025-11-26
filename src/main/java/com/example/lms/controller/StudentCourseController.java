package com.example.lms.controller;

import com.example.lms.dto.StudentCourse;
import com.example.lms.dto.Student;
import com.example.lms.service.StudentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentService studentService;

    // 수강신청 페이지 (검색 + 페이징)
    @GetMapping("/addCourse")
    public String addCoursePage(HttpSession session, Model model,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page) {

        Student loginStudent = (Student) session.getAttribute("loginStudent");
        if (loginStudent == null) return "redirect:/login";

        int studentNo = loginStudent.getStudentNo();
        int pageSize = 10; // 한 페이지에 표시할 강의 수

        List<StudentCourse> courses;
        int totalCourses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = studentService.searchAvailableCourses(studentNo, keyword, page, pageSize);
            totalCourses = studentService.countSearchAvailableCourses(studentNo, keyword);
        } else {
            courses = studentService.getAvailableCoursesWithTimes(studentNo, page, pageSize);
            totalCourses = studentService.countAvailableCourses(studentNo);
        }

        int totalPages = (int) Math.ceil((double) totalCourses / pageSize);

        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "student/addCourse";
    }

    // 수강신청 처리
    @PostMapping("/addCourse/{courseNo}")
    public String addCourse(@PathVariable int courseNo, HttpSession session, Model model) {
        Student loginStudent = (Student) session.getAttribute("loginStudent");
        if (loginStudent == null) return "redirect:/login";

        int studentNo = loginStudent.getStudentNo();
        try {
            studentService.applyCourse(studentNo, courseNo);
            model.addAttribute("msg", "수강신청이 완료되었습니다.");
        } catch (Exception e) {
            model.addAttribute("msg", e.getMessage());
        }

        // 신청 후 첫 페이지 재조회
        List<StudentCourse> courses = studentService.getAvailableCoursesWithTimes(studentNo, 1, 10);
        int totalCourses = studentService.countAvailableCourses(studentNo);
        int totalPages = (int) Math.ceil((double) totalCourses / 10);

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", totalPages);

        return "student/addCourse";
    }
}
