package com.example.lms.controller;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


import com.example.lms.dto.StudentCourse;
import com.example.lms.dto.Assignment;
import com.example.lms.dto.Project;
import com.example.lms.dto.ProjectResult;
import com.example.lms.dto.Question;
import com.example.lms.dto.Student;
import com.example.lms.dto.TimetableCell;
import com.example.lms.service.StudentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.http.HttpHeaders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return "student/studentSchedule";
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

    // 수강 신청 페이지
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

        List<StudentCourse> courses = studentService.getAvailableCoursesWithTimes(studentNo, 1, 10);
        int totalCourses = studentService.countAvailableCourses(studentNo);
        int totalPages = (int) Math.ceil((double) totalCourses / 10);

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", totalPages);

        return "student/addCourse";
    }
    
    //과제 상세보기
    @GetMapping("/assignment/{projectNo}")
    public String assignmentDetail(
            @PathVariable int projectNo,
            @SessionAttribute("loginStudent") Student loginStudent,
            Model model) {

        if (loginStudent == null) return "redirect:/login";

        int studentNo = loginStudent.getStudentNo();

        Project project = studentService.getProjectByProjectNo(projectNo);
        ProjectResult submitted = studentService.getProjectResult(projectNo, studentNo);

        model.addAttribute("project", project);
        model.addAttribute("submitted", submitted);

        // 수정 가능 여부 체크
        boolean editable = submitted != null && isBeforeDeadline(project.getProjectDeadline());
        model.addAttribute("editable", editable);

        return "student/projectResultOne";
    }

 // ===============================
//  과제 제출 처리
// ===============================
@PostMapping("/assignment/{projectNo}")
public String submitAssignment(
        @PathVariable int projectNo,
        @RequestParam("submissionFile") MultipartFile file,
        @RequestParam("resultTitle") String title,
        @RequestParam("resultContents") String contents,
        @SessionAttribute("loginStudent") Student loginStudent,
        RedirectAttributes redirectAttributes) {

    if (loginStudent == null) return "redirect:/login";

    Project project = studentService.getProjectByProjectNo(projectNo);

    if (!isBeforeDeadline(project.getProjectDeadline())) {
        redirectAttributes.addFlashAttribute("error", "마감일이 지나 제출할 수 없습니다.");
        return "redirect:/student/assignment/" + projectNo;
    }

    // MultipartFile 그대로 서비스로 전달
    studentService.submitAssignment(projectNo, loginStudent.getStudentNo(), file, title, contents);

    redirectAttributes.addFlashAttribute("msg", "과제가 성공적으로 제출되었습니다.");
    int courseNo = studentService.getCourseNoByProjectNo(projectNo);

    return "redirect:/student/courseOne/" + courseNo;
}

// ===============================
//  과제 다운로드
// ===============================
@GetMapping("/assignment/download/{resultNo}")
public ResponseEntity<Resource> downloadAssignment(
        @PathVariable int resultNo,
        @SessionAttribute("loginStudent") Student loginStudent) {

    if (loginStudent == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    ProjectResult result = studentService.getProjectResultByResultNo(resultNo);
    if (result == null || !result.getStudentNo().equals(loginStudent.getStudentNo())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // 업로드 경로와 동일하게 맞춤
    Path filePath = Paths.get("C:/lms/uploads/assignments", result.getResultFile());

    if (!Files.exists(filePath)) {
        System.out.println("파일 없음: " + filePath.toAbsolutePath());
        return ResponseEntity.notFound().build();
    }

    Resource resource = new FileSystemResource(filePath.toFile());

    // 파일명 한글/공백 인코딩
    String encodedFileName;
    try {
        encodedFileName = URLEncoder.encode(result.getResultFile(), "UTF-8").replaceAll("\\+", "%20");
    } catch (Exception e) {
        encodedFileName = result.getResultFile();
    }

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
            .body(resource);
}


// ===============================
//  과제 수정 처리
// ===============================
@PostMapping("/assignment/edit/{resultNo}")
public String updateAssignment(
        @PathVariable int resultNo,
        @RequestParam("submissionFile") MultipartFile file,
        @RequestParam("resultTitle") String title,
        @RequestParam("resultContents") String contents,
        @SessionAttribute("loginStudent") Student loginStudent,
        RedirectAttributes redirectAttributes) {

    if (loginStudent == null) return "redirect:/login";

    ProjectResult origin = studentService.getProjectResultByResultNo(resultNo);
    Project project = studentService.getProjectByProjectNo(origin.getProjectNo());

    if (!isBeforeDeadline(project.getProjectDeadline())) {
        redirectAttributes.addFlashAttribute("error", "마감일이 지나 수정할 수 없습니다.");
        return "redirect:/student/assignment/" + project.getProjectNo();
    }

    studentService.updateAssignment(resultNo, file, title, contents);

    redirectAttributes.addFlashAttribute("msg", "과제가 수정되었습니다.");
    return "redirect:/student/assignment/" + project.getProjectNo();
}

// ===============================
// 공통 - 마감일 비교
// ===============================
private boolean isBeforeDeadline(String projectDeadline) {
    LocalDate deadlineDate = LocalDate.parse(projectDeadline);
    LocalDateTime deadline = deadlineDate.atTime(23, 59, 59);
    return LocalDateTime.now().isBefore(deadline);
}




	// 수강 과목 상세보기
@GetMapping("/courseOne/{courseNo}")
public String courseDetail(
        @PathVariable int courseNo,
        @SessionAttribute("loginStudent") Student loginStudent,
        Model model) {

    int studentNo = loginStudent.getStudentNo();
    StudentCourse course = studentService.getCourseDetail(studentNo, courseNo);
    List<Assignment> assignmentList = studentService.getAssignments(courseNo);
    boolean cancelable = studentService.isCancelable(studentNo, courseNo);

    //  과제 마감 여부 계산
    LocalDate today = LocalDate.now();
    for (Assignment a : assignmentList) {
        LocalDate deadline = LocalDate.parse(a.getProjectDeadline());
        a.setClosed(today.isAfter(deadline));
    }

    model.addAttribute("course", course);
    model.addAttribute("assignmentList", assignmentList);
    model.addAttribute("cancelable", cancelable ? "Y" : null);

    return "student/courseOne";
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

 // 질문 작성/수정 페이지 (GET)
 @GetMapping({"/questionWrite/{courseNo}", "/questionWrite/{courseNo}/{questionNo}"})
 public String questionWritePage(
         @PathVariable int courseNo,
         @PathVariable(required = false) Integer questionNo,
         @SessionAttribute("loginStudent") Student loginStudent,
         Model model) {

     int studentNo = loginStudent.getStudentNo();

     // 강의 정보
     StudentCourse course = studentService.getCourseDetail(studentNo, courseNo);
     model.addAttribute("courseNo", course.getCourseNo());
     model.addAttribute("courseName", course.getCourseName());

     Question question;

     if (questionNo != null) {
         // 수정용 질문 불러오기
         question = studentService.getQuestionByNo(questionNo);

         if (question == null || question.getStudentNo() != studentNo) {
             return "redirect:/student/questionList";
         }

         if (question.getQuestionTitle() == null) question.setQuestionTitle("");
         if (question.getQuestionContents() == null) question.setQuestionContents("");

         // 수정 모드 표시
         model.addAttribute("isEdit", true);

     } else {
         // 신규 질문 작성용 빈 객체
         question = new Question();
         question.setCourseNo(courseNo);
         question.setQuestionTitle("");     
         question.setQuestionContents("");  

         // 작성 모드 표시
         model.addAttribute("isEdit", false);
     }

     model.addAttribute("question", question);
     return "student/questionWrite";
 }

//질문 작성/수정 처리 (POST)
@PostMapping({"/questionWrite/{courseNo}", "/questionWrite/{courseNo}/{questionNo}"})
public String submitQuestion(
      @PathVariable int courseNo,
      @PathVariable(required = false) Integer questionNo,
      @SessionAttribute("loginStudent") Student loginStudent,
      @RequestParam String questionTitle,
      @RequestParam String questionContents,
      RedirectAttributes redirectAttributes) {  

  int studentNo = loginStudent.getStudentNo();
  int empNo = studentService.getProfessorNoByCourseNo(courseNo);

  Question question = new Question();
  question.setStudentNo(studentNo);
  question.setEmpNo(empNo);
  question.setCourseNo(courseNo);
  question.setQuestionTitle(questionTitle);
  question.setQuestionContents(questionContents);

  if (questionNo != null && questionNo > 0) {
      question.setQuestionNo(questionNo);
      studentService.updateQuestion(question);
      redirectAttributes.addFlashAttribute("msg", "질문이 성공적으로 수정되었습니다."); // 수정 메시지
  } else {
      studentService.insertQuestion(question);
      redirectAttributes.addFlashAttribute("msg", "질문이 성공적으로 작성되었습니다."); // 작성 메시지
  }

  return "redirect:/student/courseOne/" + courseNo;
}

    // 질문 목록
    @GetMapping("/questionList")
    public String questionList(@SessionAttribute("loginStudent") Student loginStudent, Model model) {
        int studentNo = loginStudent.getStudentNo();
        List<Question> questions = studentService.getQuestionsByStudent(studentNo);
        model.addAttribute("questions", questions);
        return "student/questionList";
    }

    // 질문 상세보기
    @GetMapping("/questionOne/{questionNo}")
    public String questionOne(
            @PathVariable int questionNo,
            @SessionAttribute("loginStudent") Student loginStudent,
            Model model) {

        Question question = studentService.getQuestionByNo(questionNo);

        if (question == null || question.getStudentNo() != loginStudent.getStudentNo()) {
            return "redirect:/student/questionList";
        }

        model.addAttribute("question", question);
        return "student/questionOne";
    }

    // 질문 삭제 처리
    @PostMapping("/questionDelete/{questionNo}")
    public String deleteQuestion(
            @PathVariable int questionNo,
            @SessionAttribute("loginStudent") Student loginStudent) {

        Question question = studentService.getQuestionByNo(questionNo);

        if (question != null && question.getStudentNo() == loginStudent.getStudentNo()) {
            studentService.deleteQuestion(questionNo);
        }

        return "redirect:/student/questionList";
    }
}
