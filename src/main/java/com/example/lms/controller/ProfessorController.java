package com.example.lms.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.lms.dto.AjaxResult;
import com.example.lms.dto.Attendance;
import com.example.lms.dto.AttendanceHistory;
import com.example.lms.dto.AttendanceSummary;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseStudent;
import com.example.lms.dto.CourseTime;
import com.example.lms.dto.CourseWithTime;
import com.example.lms.dto.Dept;
import com.example.lms.dto.Emp;
import com.example.lms.dto.PageInfo;
import com.example.lms.dto.Project;
import com.example.lms.dto.ProjectResult;
import com.example.lms.dto.Question;
import com.example.lms.dto.Score;
import com.example.lms.dto.Student;
import com.example.lms.dto.StudentScorePF;
import com.example.lms.dto.TimetableCell;
import com.example.lms.service.DeptService;
import com.example.lms.service.ProfessorService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;



@Slf4j
@Controller
public class ProfessorController {
	@Autowired
	ProfessorService professorService;
	@Autowired
	DeptService deptService;
	
	// 파일 위치, 확장자
	private final String uploadDir = "C:/lms/uploads";
	
	// 최종 성적 수정
	@PostMapping("/professor/modifyFinalScore")
	public String modifyFinalScore(HttpSession session, Score score, String courseName) {
		Emp loginProfessor = getLoginProfessor(session);
		log.debug("studentNo={}, scoreGrade={}", score.getStudentNo(), score.getScoreGrade());
		professorService.modifyFinalScore(score);
		String encodedCourseName = URLEncoder.encode(courseName, StandardCharsets.UTF_8);
		return "redirect:/professor/scoreList?courseName=" + encodedCourseName + "&courseNo=" + score.getCourseNo();
	}
		
	// 성적 목록
	@GetMapping("/professor/scoreList")
	public String scoreList(HttpSession session, Model model, int courseNo, String courseName) {
		Emp loginProfessor = getLoginProfessor(session);
		List<Score> scoreList = professorService.getScoreByCourse(courseNo);
		scoreList.forEach(score -> score.setCourseName(courseName));
		model.addAttribute("courseName", courseName);
		model.addAttribute("scoreList", scoreList);
		log.debug("courseName :" + courseName);
		log.debug("scoreList :" + scoreList);
		return "professor/scoreList";
	}
	
	// 성적 목록(강의선택)
	@GetMapping("/professor/courseScoreList")
	public String courseScoreList(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);
		List<Course> courseList = professorService.getCourseAttandanceAndScore(loginProfessor.getEmpNo());
		model.addAttribute("courseList", courseList);
		return "professor/courseScoreList";
	}
	
	// 성적 등록 폼
	@GetMapping("/professor/addScore")
	public String addScore(HttpSession session, Model model,int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);
		List<StudentScorePF> studentList = professorService.getStudentListAndScore(courseNo);
		
			
	    model.addAttribute("studentList", studentList);
	    model.addAttribute("courseNo", courseNo);
		
		return "professor/addScore";
	}

	// 성적 등록 액션
	@PostMapping("/professor/addScore")
	public String addScore(HttpSession session, Score score, RedirectAttributes redirectAttributes) {
		Emp loginProfessor = getLoginProfessor(session);
		score.setEmpNo(loginProfessor.getEmpNo());
		
		// 학생별 existsScore 확인
        StudentScorePF student = professorService.getStudentListAndScore(score.getCourseNo())
                .stream()
                .filter(s -> s.getStudentNo() == score.getStudentNo())
                .findFirst()
                .orElse(null);
		
        if (student != null && student.isExistsScore()) {
            redirectAttributes.addFlashAttribute("msg", "이미 등록된 성적입니다.");
        } else {
            // 총점 계산
            double scoreTotal = score.getScoreAtt() + score.getScoreProject() + score.getScoreMid() + score.getScoreFin();
            score.setScoreTotal(scoreTotal);

            // 임시 등급
            score.setScoreGrade(score.getScoreAtt() == 0 ? "F" : "X");

            professorService.addScore(score);

            // 상위 30% 등급 재계산
            List<Score> allScores = professorService.getScoreByCourse(score.getCourseNo());
            professorService.assignGrades(allScores);
        }
		
		return "redirect:/professor/addScore?courseNo=" + score.getCourseNo();
	}
		
	// 성적 등록(강의목록)
	@GetMapping("/professor/courseScoreRegister")
	public String score(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);
		List<Course> courseList = professorService.getCourseAttandanceAndScore(loginProfessor.getEmpNo());
		model.addAttribute("courseList", courseList);
		return "professor/courseScoreRegister";
	}
	
	// 답변 등록
	@PostMapping("/professor/addAnswer")
	@ResponseBody
	public AjaxResult addAnswer(HttpSession session, @RequestBody Question question) {
		Emp loginProfessor = getLoginProfessor(session);
        question.setEmpNo(loginProfessor.getEmpNo());
        question.setAnswerStatus("Y"); // 답변 완료 처리

        int row = professorService.updateAnswer(question); // answerContents만 DB UPDATE

        AjaxResult result = new AjaxResult();
        result.setSuccess(row == 1);
        return result; // JSON으로 AJAX에 반환
    }	
	
	// 문의사항 목록
	@GetMapping("/professor/questionList")
	public String questionList(HttpSession session, Model model, @RequestParam(defaultValue = "1") int currentPage) {
		Emp loginProfessor = getLoginProfessor(session);
		
		int empNo = loginProfessor.getEmpNo();
		int rowPerPage = 10; 
	    int pageBlock = 10; 
	     
		List<Question> questionList = professorService.questionListByPage(currentPage, empNo);
		Integer totalCount = professorService.getQuestionCount(empNo);
		int total = (totalCount != null) ? totalCount : 0;
		
	    PageInfo pageInfo = getPageInfo(total, currentPage, rowPerPage, pageBlock);
	    		
	    List<Map<String,Object>> pageListWithCurrent = new ArrayList<>();
	    for(int p : pageInfo.getPageList()) {
	        Map<String,Object> m = new HashMap<>();
	        m.put("page", p);
	        m.put("isCurrent", p == pageInfo.getCurrentPage());
	        pageListWithCurrent.add(m);
	    }
	    
	 
	    
	    model.addAttribute("questionList", questionList);
	    model.addAttribute("pageInfo", pageInfo);
	    model.addAttribute("pageList", pageListWithCurrent);
	    
	 		
		return "professor/questionList";
	}
	
	// 과제 점수 등록하기
	@PostMapping("/professor/addResultScore")
	public String addResultScore(HttpSession session, ProjectResult pr) {
		Emp loginProfessor = getLoginProfessor(session);
		Integer resultScore = pr.getResultScore();
	    if(resultScore != null) {
	        if(resultScore < 0) resultScore = 0;
	        if(resultScore > 20) resultScore = 20;
	        pr.setResultScore(resultScore);
	    }
		
	    professorService.addResultScore(pr);
		return "redirect:/professor/projectResultList?projectNo=" + pr.getProjectNo();
	}
	
	// 과제 결과물 상세보기
	@GetMapping("/professor/projectResultOne")
	public String projectResultOne(HttpSession session, Model model, int resultNo) {
		Emp loginProfessor = getLoginProfessor(session);
				
		ProjectResult pr = professorService.projectResultOne(resultNo);
		model.addAttribute("pr", pr);
				
		return "professor/projectResultOne";
	}
	
	// 과제 결과물 목록
	@GetMapping("/professor/projectResultList")
	public String projectResultList(HttpSession session, Model model, int projectNo) {
		Emp loginProfessor = getLoginProfessor(session);
		List<ProjectResult> resultList = professorService.projectResultList(projectNo);	    
	    model.addAttribute("resultList", resultList);
		
		return "professor/projectResultList";
	}
	
	// 과제 삭제
	@PostMapping("/professor/removeProject")
	public String deleteProjectIfNoResults(HttpSession session, int projectNo) {
		Emp loginProfessor = getLoginProfessor(session);
		professorService.deleteProjectIfNoResults(projectNo);
		return "redirect:/professor/projectList";
	}
	
	// 과제 등록 폼
	@GetMapping("/professor/addProject") 
	public String addProject(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);
		List<Course> courseList = professorService.getCourseAttandanceAndScore(loginProfessor.getEmpNo());
		model.addAttribute("courseList", courseList);
		
		return "professor/addProject";
	}

	// 과제 등록 액션
	@PostMapping("/professor/addProject") 
	public String addProject(HttpSession session, Project p) {
		Emp loginProfessor = getLoginProfessor(session);
		p.setEmpNo(loginProfessor.getEmpNo());
		professorService.addProject(p);
		
		return "redirect:/professor/projectList";
	}
	
	// 과제목록
	@GetMapping("/professor/projectList")
	public String projectList(HttpSession session, Model model,Project p, @RequestParam(defaultValue = "1") int currentPage) {
		Emp loginProfessor = getLoginProfessor(session);
		
		int rowPerPage = 10;       // 한 페이지에 표시할 강의 수
	    int pageBlock = 10;        // 한 블록에 표시할 페이지 수

	    // 강의 리스트
	    List<Project> projectList = professorService.projectListByPage(loginProfessor.getEmpNo(), currentPage);

	    // 전체 강의 수
	    int totalCount = professorService.getProjectCount(loginProfessor.getEmpNo());

	    // PageInfo 생성
	    PageInfo pageInfo = getPageInfo(totalCount, currentPage, rowPerPage, pageBlock);

	    // Mustache에서 편하게 쓰기 위해 pageList에 isCurrent 표시
	    List<Map<String,Object>> pageListWithCurrent = new ArrayList<>();
	    for(int page : pageInfo.getPageList()) {
	        Map<String,Object> m = new HashMap<>();
	        m.put("page", page);
	        m.put("isCurrent", page == pageInfo.getCurrentPage());
	        pageListWithCurrent.add(m);
	    }
	   
	    model.addAttribute("projectList", projectList);
	    model.addAttribute("pageInfo", pageInfo);
	    model.addAttribute("pageList", pageListWithCurrent);
		return "professor/projectList";
	}
	
	// 출석수정 폼
	@GetMapping("/professor/modifyHistory")
	public String updateHistory(HttpSession session, Model model, int historyNo) {
		Emp loginProfessor = getLoginProfessor(session);
		
		AttendanceHistory ah = professorService.getHistoryByHN(historyNo);
		
		model.addAttribute("ah", ah);
		
				
		return "professor/modifyHistory";
	}
	
	// 출석수정 액션
	@PostMapping("/professor/modifyHistory")
	public String updateHistory(HttpSession session, AttendanceHistory ah) {
		Emp loginProfessor = getLoginProfessor(session);
		
		// 새 파일 업로드 처리
		MultipartFile newFile = ah.getNewFile();
	    if (newFile != null && !newFile.isEmpty()) {
	        
	    	String originalName = newFile.getOriginalFilename(); // 원본 파일명
	    	String fileName = UUID.randomUUID().toString() + "_" + originalName;

	        File saveFile = new File(uploadDir, fileName);

	        try {
	            newFile.transferTo(saveFile);
	        } catch (Exception e) {
	            throw new RuntimeException("파일 추가 실패");
	        }

	        // DTO에 새 파일명 저장
	        ah.setHistoryFile(fileName);
	        ah.setHistoryFileOriginal(originalName);
	    }
	    	   
	    // 데이터 수정
	    professorService.updateHistory(ah);
	    professorService.updateAttendanceFromUpdateHistory(ah);
	    
		
		return "redirect:/professor/attendanceHistoryList";
	}
	
	// 다운로드 파일
	@GetMapping("/professor/downloadHistoryFile")
	public void downloadHistoryFile(@RequestParam("fileName") String fileName, HttpServletResponse response) throws IOException {
		 File file = new File(uploadDir, fileName);

		    if (!file.exists()) {
		        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        return;
		    }

		    // 1. MIME 타입 추출
		    String mimeType = Files.probeContentType(file.toPath());
		    if (mimeType == null) mimeType = "application/octet-stream";
		    response.setContentType(mimeType);

		    // 2. 브라우저에서 바로 열 수 있는 타입
		    Set<String> inlineMimeTypes = Set.of(
		        "application/pdf",
		        "image/png",
		        "image/jpeg",
		        "image/gif",
		        "text/plain",
		        "text/html"
		    );

		    // 3. 파일명 인코딩
		    String encodedFileName = URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");

		    // 4. Content-Disposition 결정
		    if (inlineMimeTypes.contains(mimeType)) {
		        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
		    } else {
		        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
		    }

		    // 5. 파일 스트리밍
		    try (FileInputStream fis = new FileInputStream(file);
		         OutputStream os = response.getOutputStream()) {

		        byte[] buffer = new byte[1024];
		        int bytesRead;
		        while ((bytesRead = fis.read(buffer)) != -1) {
		            os.write(buffer, 0, bytesRead);
		        }
		        os.flush();
		    }
	}
	
	// 출석내역목록
	@GetMapping("/professor/attendanceHistoryList")
	public String attendanceHistoryList(
	        HttpSession session, 
	        Model model,
	        @RequestParam(defaultValue = "1") int currentPage, 
	        @RequestParam(required = false) String studentName, 
	        @RequestParam(required = false) Integer courseNo) {

	    Emp loginProfessor = getLoginProfessor(session);

	    int rowPerPage = 10; 
	    int pageBlock = 10; 

	    // Map에 null도 넣어줘야 MyBatis에서 key 오류 안 남
	    Map<String, Object> param = new HashMap<>();
	    param.put("courseNo", courseNo);
	    param.put("studentName", (studentName != null && !studentName.isEmpty()) ? studentName.trim() : null);
	    param.put("beginRow", (currentPage - 1) * rowPerPage);
	    param.put("rowPerPage", rowPerPage);

	    List<AttendanceHistory> historyList = professorService.attendanceHistoryList(param);
	    Integer totalCount = professorService.getHistoryCount(param);
	    int total = (totalCount != null) ? totalCount : 0;

	    PageInfo pageInfo = getPageInfo(total, currentPage, rowPerPage, pageBlock);

	    List<Map<String,Object>> pageListWithCurrent = new ArrayList<>();
	    for(int p : pageInfo.getPageList()) {
	        Map<String,Object> m = new HashMap<>();
	        m.put("page", p);
	        m.put("isCurrent", p == pageInfo.getCurrentPage());
	        pageListWithCurrent.add(m);
	    }

	    model.addAttribute("historyList", historyList);
	    model.addAttribute("studentName", studentName != null ? studentName : "");
	    model.addAttribute("courseNo", courseNo != null ? courseNo : "");
	    model.addAttribute("pageInfo", pageInfo);
	    model.addAttribute("pageList", pageListWithCurrent);

	    return "professor/attendanceHistoryList";
	}
	
	// 출석체크 폼
	@GetMapping("/professor/addAttendance")
	public String addAttendance(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);

	    // 학생 + 출석 요약
	    List<CourseStudent> studentList = professorService.getStudentListByCourse(courseNo);
	    
	    LocalDate today = LocalDate.now();

	    for (CourseStudent student : studentList) {
	        boolean alreadyChecked = professorService.isAttendanceCheckedToday(student.getStudentNo(), courseNo, today);
	        student.setAlreadyChecked(alreadyChecked);
	    }
	    
	    log.debug("studentList : " + studentList);
	    model.addAttribute("studentList", studentList);
	    model.addAttribute("courseNo", courseNo);

	    return "professor/addAttendance";
	}
	
	// 출석체크 액션
	@PostMapping("/professor/addAttendance")
	public String addAttendance(HttpSession session, @RequestParam("studentNo") int[] studentNos,
	        @RequestParam("attState") String[] attStates, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);
		
		for (int i = 0; i < studentNos.length; i++) {
	        Attendance a = new Attendance();
	        a.setStudentNo(studentNos[i]);
	        a.setAttState(attStates[i]);
	        a.setCourseNo(courseNo);
		    a.setEmpNo(loginProfessor.getEmpNo());
		
		    professorService.insertAttendance(a);
		    professorService.insertHistoryFromAddAttendance(a);
		}   
		
		return "redirect:/professor/attendance";
	}
	
	// 출석체크(강의목록)
	@GetMapping("/professor/attendance")
	public String attendance(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);
		List<Course> courseList = professorService.getCourseAttandanceAndScore(loginProfessor.getEmpNo());
		model.addAttribute("courseList", courseList);
		log.debug("courseList : " + courseList);
		return "professor/attendance";
	}
	
	// 학생리스트
	@GetMapping("/professor/studentList")
	public String studentList(HttpSession session, Model model, @RequestParam(defaultValue = "1") int currentPage, @RequestParam(defaultValue = "") String searchWord) {
		Emp loginProfessor = getLoginProfessor(session);
		
		int rowPerPage = 10;       // 한 페이지에 표시할 강의 수
	    int pageBlock = 10;        // 한 블록에 표시할 페이지 수

	    // 학생 리스트
	    List<Student> studentList = professorService.studentListByPage(loginProfessor.getDeptNo(), currentPage, searchWord);
	    	    
	    // 전체 학생 수
	    int totalCount = professorService.getStudentCount(loginProfessor.getDeptNo(), searchWord);

	    // PageInfo 생성
	    PageInfo pageInfo = getPageInfo(totalCount, currentPage, rowPerPage, pageBlock);

	    List<Map<String,Object>> pageListWithCurrent = new ArrayList<>();
	    for(int p : pageInfo.getPageList()) {
	        Map<String,Object> m = new HashMap<>();
	        m.put("page", p);
	        m.put("isCurrent", p == pageInfo.getCurrentPage());
	        pageListWithCurrent.add(m);
	    }
	    for (Student s : studentList) {
	        String img = s.getStudentImg() != null ? s.getStudentImg() : "";
	    } 
	    	 
	    model.addAttribute("studentList", studentList);
	    model.addAttribute("searchWord", searchWord);
	    model.addAttribute("pageInfo", pageInfo);
	    model.addAttribute("pageList", pageListWithCurrent);
	    
		return "professor/studentList";
	}
	
	// 강의 상세보기
	@GetMapping("/professor/courseOne")
	public String courseOne(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);
		
		Course course = professorService.getCourseOne(courseNo);
		List<CourseTime> courseTimeList = professorService.getCourseTimeList(courseNo);
		    
	    model.addAttribute("course", course);
	    model.addAttribute("courseTimeList", courseTimeList);
		return "professor/courseOne";
	}
	
	
	// 강의 수정 폼
	@GetMapping("/professor/modifyCourse")
	public String courseUpdate(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);
		
		Course c = professorService.getCourseOne(courseNo);
		List<CourseTime> courseTimeList = professorService.getCourseTime(courseNo);
		
		// 요일 목록
	    String[] days = {"월","화","수","목","금"};
	    
	    int index = 0;
	    // 각 CourseTime에 맞춘 dayOptions 생성
	    for (CourseTime ct : courseTimeList) {
	        StringBuilder dayOptions = new StringBuilder();
	        ct.setIndex(index);  // Mustache에서 {{index}} 사용
	        index++;
	        for (String day : days) {
	            if (day.equals(ct.getCoursedate())) {
	                dayOptions.append("<option value='")
	                          .append(day)
	                          .append("' selected>")
	                          .append(day)
	                          .append("</option>");
	            } else {
	                dayOptions.append("<option value='")
	                          .append(day)
	                          .append("'>")
	                          .append(day)
	                          .append("</option>");
	            }
	        }

	        ct.setDayOptions(dayOptions.toString());  // ★ CourseTime에 저장
	    
	    }
	    
	    CourseWithTime cwt = new CourseWithTime();
	    cwt.setCourse(c); // 기존 course

	    // 기존 값을 새 객체에 복사
	    Course newCourse = new Course();
	    newCourse.setCourseNo(c.getCourseNo());
	    newCourse.setCourseName(c.getCourseName());
	    newCourse.setDeptNo(c.getDeptNo());
	    newCourse.setMaxCnt(c.getMaxCnt());
	    newCourse.setCoursePlan(c.getCoursePlan());
	    newCourse.setCoursePeriod(c.getCoursePeriod());

	    cwt.setNewCourse(newCourse); // 이제 newCourse는 기존 값 복사 + 별도 객체
	    cwt.setCourseTimes(courseTimeList);

	    model.addAttribute("cwt", cwt);
			
		return "professor/modifyCourse";
	}
	
	@PostMapping("/professor/modifyCourse")
	public String courseUpdate(HttpSession session, CourseWithTime cwt, RedirectAttributes redirectAttrs) {
	    Emp loginProfessor = getLoginProfessor(session);

	    // newCourse가 null이면 새 객체로 초기화
	    if (cwt.getNewCourse() == null) {
	        cwt.setNewCourse(new Course());
	    }

	    Course updatedCourse = cwt.getNewCourse();

	    // PK가 없으면 기존 값 세팅
	    if (updatedCourse.getCourseNo() == 0 && cwt.getCourse() != null) {
	        updatedCourse.setCourseNo(cwt.getCourse().getCourseNo());
	    }

	    // coursePeriod -> LocalDate 변환 안전 처리
	    LocalDate courseStartDate = null;
	    LocalDate courseEndDate = null;
	    if (updatedCourse.getCoursePeriod() != null && updatedCourse.getCoursePeriod().contains("~")) {
	        String[] periodParts = updatedCourse.getCoursePeriod().split("~");
	        if (periodParts.length == 2) {
	            String startStr = periodParts[0].trim();
	            String endStr = periodParts[1].trim();
	            if (!startStr.isBlank() && !endStr.isBlank()) {
	                courseStartDate = LocalDate.parse(startStr);
	                courseEndDate = LocalDate.parse(endStr);
	            }
	        }
	    }

	    // 기존 강의시간 조회 (자기 강의 제외)
	    List<CourseTime> existingTimes = professorService.getCourseTimesByEmp(loginProfessor.getEmpNo())
	        .stream()
	        .filter(ct -> ct.getCourseNo() != updatedCourse.getCourseNo())
	        .collect(Collectors.toList());

	    // 새 CourseTime에 기간 세팅
	    for (CourseTime ct : cwt.getCourseTimes()) {
	        ct.setCourseStartDate(courseStartDate);
	        ct.setCourseEndDate(courseEndDate);
	    }

	    // 시간 겹침 체크
	    CourseTime overlap = checkCourseTimeOverlap(cwt.getCourseTimes(), existingTimes);
	    if (overlap != null) {
	        redirectAttrs.addFlashAttribute("msg",
	            "강의시간이 기존 강의와 겹칩니다: "
	            + overlap.getCoursedate() + " "
	            + overlap.getCourseTimeStart() + "-" 
	            + overlap.getCourseTimeEnd());
	        return "redirect:/professor/modifyCourse?courseNo=" + updatedCourse.getCourseNo();
	    }
	    
	    // 강의 업데이트
	    professorService.updateCourse(updatedCourse);

	    // 기존 강의시간 삭제 후 새로 등록
	    professorService.deleteCourseTime(updatedCourse.getCourseNo());
	    for (CourseTime ct : cwt.getCourseTimes()) {
	        ct.setCourseNo(updatedCourse.getCourseNo());
	        professorService.addCourseTime(ct);
	    }

	    return "redirect:/professor/courseOne?courseNo=" + updatedCourse.getCourseNo();
	}
	
	// 강의 삭제
	@PostMapping("/professor/removeCourse")
	public String courseDelete(HttpSession session, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);

		professorService.deleteCourseTime(courseNo);
	    professorService.deleteCourse(courseNo);
	    
	    return "redirect:/professor/courseList";
	}
		
	// 강의 등록 폼
	@GetMapping("/professor/addCourse")
	public String addCourse(HttpSession session) {
		Emp loginProfessor = getLoginProfessor(session);
		return "professor/addCourse";
	}
	
	// 강의 등록 액션
	@PostMapping("/professor/addCourse")
	public String addCourse(HttpSession session, CourseWithTime cwt, RedirectAttributes redirectAttrs) {	
	    Emp loginProfessor = getLoginProfessor(session);

	    cwt.getCourse().setEmpNo(loginProfessor.getEmpNo());
	    
	    List<CourseTime> existingTimes =
	            professorService.getCourseTimesByEmp(loginProfessor.getEmpNo());

	    CourseTime overlap =
	            checkCourseTimeOverlap(cwt.getCourseTimes(), existingTimes);

	    if (overlap != null) {
	        redirectAttrs.addFlashAttribute("msg",
	            "강의시간이 기존 강의와 겹칩니다: "
	            + overlap.getCoursedate() + " "
	            + overlap.getCourseTimeStart() + " ~ "
	            + overlap.getCourseTimeEnd());

	        return "redirect:/professor/addCourse"; //✅ 등록 완료
	    }

	    professorService.insertCourse(cwt.getCourse());
	    professorService.insertCourseWithTimes(cwt);

	    return "redirect:/professor/courseList?currentPage=1";
	}

	
	// 요일 + 시간 겹침 체크 함수
	private CourseTime checkCourseTimeOverlap(List<CourseTime> newTimes, List<CourseTime> existingTimes) {
	    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

	    for (CourseTime newTime : newTimes) {

	        // "09:00 ~ 10:35" 형식 대비
	        String startStr = newTime.getCourseTimeStart().contains("~")
	                ? newTime.getCourseTimeStart().split("~")[0].trim()
	                : newTime.getCourseTimeStart();

	        String endStr = newTime.getCourseTimeEnd().contains("~")
	                ? newTime.getCourseTimeEnd().split("~")[1].trim()
	                : newTime.getCourseTimeEnd();

	        LocalTime newStart = LocalTime.parse(startStr, timeFormatter);
	        LocalTime newEnd   = LocalTime.parse(endStr, timeFormatter);

	        for (CourseTime existTime : existingTimes) {

	            LocalTime existStart = LocalTime.parse(existTime.getCourseTimeStart(), timeFormatter);
	            LocalTime existEnd   = LocalTime.parse(existTime.getCourseTimeEnd(), timeFormatter);

	            // ✅ 1. 요일 겹침 체크
	            if (!newTime.getCoursedate().equals(existTime.getCoursedate())) {
	                continue;
	            }

	            // ✅ 2. 시간 겹침 체크 (함수 합쳐서 바로 처리)
	            if (newStart.isBefore(existEnd) && existStart.isBefore(newEnd)) {
	                return newTime; // ✅ 요일 + 시간 겹침 발생
	            }
	        }
	    }
	    return null; // ✅ 겹침 없음
	}
	
	// 강의리스트
	@GetMapping("/professor/courseList")
	public String courseList(HttpSession session, Model model, @RequestParam(defaultValue = "") String searchWord) {
		Emp loginProfessor = getLoginProfessor(session);
		
	    // 강의 리스트
	    List<Course> courseList = professorService.courseListByPage(loginProfessor.getEmpNo(), searchWord);

	    log.debug("courseList : " + courseList);

	    model.addAttribute("courseList", courseList);
	    model.addAttribute("searchWord", searchWord);
	  		
		return "professor/courseList";
	}
	
	// 교수 정보 수정 폼	
	@GetMapping("/professor/modifyProfessorInfo")
	public String modifyProfessorInfo(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);

        Emp e = professorService.professorInfo(loginProfessor.getEmpNo());
        normalizeEmpFields(e);
        
        List<Dept> deptList = deptService.getDeptList();
        boolean noDeptSelected = (loginProfessor.getDeptNo() == null);
        
        // 각 학과에 selected 필드 세팅
        for (Dept d : deptList) {
            d.setSelected(d.getDeptNo().equals(loginProfessor.getDeptNo()));
        }
        
        model.addAttribute("e", e);
        model.addAttribute("noDeptSelected", noDeptSelected);
        model.addAttribute("deptList", deptList);
	    
		return "professor/modifyProfessorInfo";
	}
	
	// 교수 정보 수정 액션
	@PostMapping("/professor/modifyProfessorInfo")
	public String modifyProfessorInfo(HttpSession session, Emp e) {
		Emp loginProfessor = getLoginProfessor(session);
	    e.setEmpNo(loginProfessor.getEmpNo());

        // 비밀번호 미입력 시 기존 DB 값 유지
        if (isEmpty(e.getEmpPw())) {
            Emp old = professorService.professorInfo(e.getEmpNo());
            e.setEmpPw(old.getEmpPw());
        }

        // 파일 업로드 처리
        handleFileUpload(e);
	        
		professorService.updateProfessorInfo(e);
		return "redirect:/professor/professorInfo";
	}
	
	// 교수정보
	@GetMapping("/professor/professorInfo")
	public String professorInfo(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);

		
        Emp e = professorService.professorInfo(loginProfessor.getEmpNo());
        // deptName 세팅
        if (e.getDeptNo() != null) {
            Dept dept = deptService.getDeptByNo(e.getDeptNo());
            if (dept != null) {
                e.setDeptName(dept.getDeptName());
            } else {
                e.setDeptName(null); // 존재하지 않으면 null
            }
        }
        
        normalizeEmpFields(e);

        model.addAttribute("e", e);
		
		return "professor/professorInfo";
	}
	
	// 교수 홈
	@GetMapping("/professor/professorHome")
	public String professorHome(HttpSession session,  Model model) {
		Emp loginProfessor = getLoginProfessor(session);
		
		// 1. 강의 시간표
	    List<TimetableCell> timetable = professorService.getFullTimetable(loginProfessor.getEmpNo());
	    model.addAttribute("timetable", timetable);
	    
	    // 2. 학사 일정 / 달력
	    // List<Map<String, String>> schedule = professorService.getProfessorSchedule();
	    // model.addAttribute("schedule", schedule);
		
		return "professor/professorHome";
	}
	
	// ===========================
    // 공통 유틸 메서드
    // ===========================
	
	// 페이징
	public PageInfo getPageInfo(int totalCount, int currentPage, int rowPerPage, int pageBlock) {
	    PageInfo pageInfo = new PageInfo();
	    pageInfo.setCurrentPage(currentPage);
	    pageInfo.setRowPerPage(rowPerPage);
	    pageInfo.setPageBlock(pageBlock);
	    pageInfo.setTotalCount(totalCount);

	    int lastPage = (int) Math.ceil((double) totalCount / rowPerPage);
	    pageInfo.setLastPage(lastPage);

	    // currentPage가 lastPage를 초과하면 lastPage로 제한
	    if(currentPage > lastPage) {
	        currentPage = lastPage > 0 ? lastPage : 1; // lastPage가 0이면 1로 설정
	    }
	    pageInfo.setCurrentPage(currentPage);
	    
	    int startPage = ((currentPage - 1) / pageBlock) * pageBlock + 1;
	    int endPage = startPage + pageBlock - 1;
	    if(endPage > lastPage) endPage = lastPage;

	    pageInfo.setStartPage(startPage);
	    pageInfo.setEndPage(endPage);
	    pageInfo.setPrePage(startPage > 1 ? startPage - 1 : null);
	    pageInfo.setNextPage(endPage < lastPage ? endPage + 1 : null);

	    List<Integer> pages = new ArrayList<>();
	    for(int i = startPage; i <= endPage; i++) pages.add(i);
	    pageInfo.setPageList(pages);

	    return pageInfo;
	}
	
    private Emp getLoginProfessor(HttpSession session) {
        Emp loginProfessor = (Emp) session.getAttribute("loginProfessor");
        if (loginProfessor == null) {
            throw new RuntimeException("로그인 세션 없음");
        }
        return loginProfessor;
    }

    private void normalizeEmpFields(Emp e) {
        e.setEmpBirth(e.getEmpBirth() == null ? "" : e.getEmpBirth().trim());
        e.setEmpPhone(e.getEmpPhone() == null ? "" : e.getEmpPhone().trim());
        e.setEmpImg(e.getEmpImg() == null ? "" : e.getEmpImg().trim());
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void handleFileUpload(Emp e) {
        MultipartFile file = e.getEmpImgFile();
        if (file != null && !file.isEmpty()) {
            String ext = getExtension(file.getOriginalFilename()).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
            if (!allowedExtensions.contains(ext)) {
                log.warn("허용되지 않은 파일 형식: {}", file.getOriginalFilename());
                return; // 기존 파일 유지
            }

            // UUID로 새 파일명 생성
            String newFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File saveFile = new File(uploadDir, newFileName);
            saveFile.getParentFile().mkdirs();

            try {
                file.transferTo(saveFile);

                // 기존 파일 삭제
                if (e.getEmpImg() != null && !e.getEmpImg().isBlank()) {
                    File oldFile = new File(uploadDir, e.getEmpImg());
                    if (oldFile.exists()) oldFile.delete();
                }

                e.setEmpImg(newFileName);
            } catch (IOException ex) {
                log.error("파일 업로드 실패", ex);
            }
        }
        // 파일 없으면 기존 이미지 그대로 유지
    }
    
    // 과제 다운로드
    @GetMapping("/professor/downloadProjectFile")
    public void downloadProjectFile(@RequestParam String fileName, HttpServletResponse response) throws IOException {
    	String uploadDir = "C:/lms/uploads/assignments/";
        File file = new File(uploadDir, fileName);

        if (file.exists()) {
            // 브라우저에서 다운로드되도록 헤더 설정
            response.setContentType("application/octet-stream");
            String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            // 파일 스트림 복사
            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
        }
    }
    
    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return (idx != -1) ? filename.substring(idx + 1) : "";
    }
}

