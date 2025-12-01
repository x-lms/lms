package com.example.lms.controller;

import com.example.lms.dto.StudentCourse;
import com.example.lms.dto.Assignment;
import com.example.lms.dto.Question;
import com.example.lms.dto.Student;
import com.example.lms.dto.TimetableCell;
import com.example.lms.service.StudentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentService studentService;

    // 학생 시간표 보기   
    @GetMapping("/timetable")
    public String studentTimetable(
            @SessionAttribute("loginStudent") Student loginStudent,
            Model model) {

        int studentNo = loginStudent.getStudentNo();
        List<TimetableCell> timetable = studentService.getStudentSchedule(studentNo);

        model.addAttribute("timetable", timetable);
        return "student/studentSchedule"; // studentSchedule.mustache
    }


    // 수강 과목 목록
    @GetMapping("/courseList")
    public String studentCourseList(
            @SessionAttribute("loginStudent") Student loginStudent,
            Model model) {

        int studentNo = loginStudent.getStudentNo();
        List<StudentCourse> courses = studentService.getStudentCourses(studentNo);

        model.addAttribute("courses", courses);
        return "student/courseList";
    }

    // 수강 신청 페이지 (검색 + 페이징)
    @GetMapping("/addCourse")
    public String addCoursePage(
            HttpSession session, Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page) {

        Student loginStudent = (Student) session.getAttribute("loginStudent");
        if (loginStudent == null) return "redirect:/login";

        int studentNo = loginStudent.getStudentNo();
        int pageSize = 10;

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


    // 수강 신청 처리
    @PostMapping("/addCourse/{courseNo}")
    public String addCourse(
            @PathVariable int courseNo, HttpSession session, Model model) {

        Student loginStudent = (Student) session.getAttribute("loginStudent");
        if (loginStudent == null) return "redirect:/login";

        int studentNo = loginStudent.getStudentNo();

        try {
            studentService.applyCourse(studentNo, courseNo);
            model.addAttribute("msg", "수강신청이 완료되었습니다.");
        } catch (Exception e) {
            model.addAttribute("msg", e.getMessage());
        }

        // 신청 후 첫 페이지 다시 조회
        List<StudentCourse> courses = studentService.getAvailableCoursesWithTimes(studentNo, 1, 10);
        int totalCourses = studentService.countAvailableCourses(studentNo);
        int totalPages = (int) Math.ceil((double) totalCourses / 10);

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", totalPages);

        return "student/addCourse";
    }
     
     
 
 // 수강 과목 상세보기
    @GetMapping("/courseOne/{courseNo}")
    public String courseDetail(
            @PathVariable int courseNo,
            @SessionAttribute("loginStudent") Student loginStudent,
            Model model) {

        int studentNo = loginStudent.getStudentNo();

        // 과목 기본 정보 (강의 + 교수 + 시간표)
        StudentCourse course = studentService.getCourseDetail(studentNo, courseNo);

        // 과제 목록
        List<Assignment> assignmentList = studentService.getAssignments(courseNo);

        

        // 취소 가능 여부 
        boolean cancelable = studentService.isCancelable(studentNo, courseNo);

        model.addAttribute("course", course);
        model.addAttribute("assignmentList", assignmentList);
        model.addAttribute("cancelable", cancelable);

        return "student/courseOne"; // courseOne.mustache
    }
    @PostMapping("/course-cancel/{courseNo}")
    public String cancelCourse(
            @PathVariable int courseNo,
            @SessionAttribute("loginStudent") Student loginStudent,
            Model model) {

        int studentNo = loginStudent.getStudentNo();

        try {
            studentService.cancelCourse(studentNo, courseNo);
            model.addAttribute("msg", "수강취소가 완료되었습니다.");
        } catch (Exception e) {
            model.addAttribute("msg", e.getMessage());
        }

        
        return "redirect:/student/courseList";

    }
 // ========================= 질문 관련 =========================

 // 질문 등록 페이지
 @GetMapping("/questionWrite/{courseNo}")
 public String questionWrite(
         @PathVariable int courseNo,
         @SessionAttribute("loginStudent") Student loginStudent,
         Model model) {

     int studentNo = loginStudent.getStudentNo();

     StudentCourse course = studentService.getCourseDetail(studentNo, courseNo);
     model.addAttribute("courseNo", course.getCourseNo());
     model.addAttribute("courseName", course.getCourseName());
     return "student/questionWrite";
 }

 // 질문 등록 처리
 @PostMapping("/questionWrite/{courseNo}")
 public String submitQuestion(
         @PathVariable int courseNo,
         @SessionAttribute("loginStudent") Student loginStudent,
         @RequestParam String questionTitle,
         @RequestParam String questionContents) {

     int studentNo = loginStudent.getStudentNo();
     int professorNo = studentService.getProfessorNoByCourseNo(courseNo);

     Question question = new Question();
     question.setStudentNo(studentNo);
     question.setProfessorNo(professorNo);
     question.setCourseNo(courseNo);
     question.setQuestionTitle(questionTitle);
     question.setQuestionContents(questionContents);

     studentService.insertQuestion(question);

     return "redirect:/student/courseOne/" + courseNo; // 작성 후 강의 상세로 이동
 }

 // 질문 목록
 @GetMapping("/questionList")
 public String questionList(@SessionAttribute("loginStudent") Student loginStudent, Model model) {
     int studentNo = loginStudent.getStudentNo();

     // Service에서 courseName까지 채워줌
     List<Question> questions = studentService.getQuestionsByStudent(studentNo);

     model.addAttribute("questions", questions);
     return "student/questionList";
 }

 // 질문 상세보기
 @GetMapping("/questionOne/{questionNo}")
 public String questionOne(
         @PathVariable int questionNo,
         Model model) {

     Question question = studentService.getQuestionByNo(questionNo); // 필요 시 서비스에 추가
     model.addAttribute("question", question);
     return "student/questionOne";
 }

 
}
