package com.example.lms.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.dto.Attendance;
import com.example.lms.dto.Course;
import com.example.lms.dto.CourseStudent;
import com.example.lms.dto.Emp;
import com.example.lms.dto.PageInfo;
import com.example.lms.dto.Student;
import com.example.lms.service.ProfessorService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ProfessorController {
	@Autowired
	ProfessorService professorService;
	
	// 파일 위치, 확장자
	private final String uploadDir = "C:/lms/uploads";
	private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
	
	// 출석체크 폼
	@GetMapping("/professor/addAttendance")
	public String attendanceList(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);
		List<CourseStudent> studentList = professorService.getStudentListByCourse(courseNo);
		model.addAttribute("studentList", studentList);
		model.addAttribute("courseNo", courseNo);
							
		return "/professor/addAttendance";
	}
	
	// 출석체크 액션
	@PostMapping("/professor/addAttendance")
	public String attendanceList(HttpSession session, Model model, Attendance a) {
		Emp loginProfessor = getLoginProfessor(session);
		a.setEmpNo(loginProfessor.getEmpNo());
		model.addAttribute("a", a);
		
		professorService.insertAttendance(a);
		
		return "redirect:/professor/addAttendance";
	}
	
	// 출석체크(강의목록)
	@GetMapping("/professor/attendance")
	public String attendance(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);
		List<Course> courseList = professorService.getAttendance(loginProfessor.getEmpNo());
		model.addAttribute("courseList", courseList);
		log.debug("courseList : " + courseList);
		return "/professor/attendance";
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
	    
		return "/professor/studentList";
	}
	
	// 강의 상세보기
	@GetMapping("/professor/courseOne")
	public String courseOne(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);
		
		Course course = professorService.getCourseOne(courseNo);
		model.addAttribute("course", course);
		return "/professor/courseOne";
	}
	
	
	// 강의 수정 폼
	@GetMapping("/professor/modifyCourse")
	public String courseUpdate(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);
		
		Course c = professorService.getCourseOne(courseNo);
		model.addAttribute("c", c);
		
		return "/professor/modifyCourse";
	}
	
	// 강의 수정 액션
	@PostMapping("/professor/modifyCourse")
	public String courseUpdate(HttpSession session, Course course) {
		Emp loginProfessor = getLoginProfessor(session);

	    professorService.updateCourse(course);

	    return "redirect:/professor/courseOne?courseNo=" + course.getCourseNo();
	}
	
	// 강의 삭제
	@PostMapping("/professor/removeCourse")
	public String courseDelete(HttpSession session, int courseNo) {
		Emp loginProfessor = getLoginProfessor(session);

	    professorService.deleteCourse(courseNo);
	    return "redirect:/professor/courseList";
	}
	
	// 강의 등록 폼
	@GetMapping("/professor/addCourse")
	public String addCourse(HttpSession session) {
		Emp loginProfessor = getLoginProfessor(session);
		return "/professor/addCourse";
	}
	
	// 강의 등록 액션
	@PostMapping("/professor/addCourse")
	public String addCourse(HttpSession session, Course c) {
		Emp loginProfessor = getLoginProfessor(session);
		
		professorService.insertCourse(c);
		
		return "redirect:/professor/courseList";
	}
	
	// 강의리스트
	@GetMapping("/professor/courseList")
	public String courseList(HttpSession session, Model model, @RequestParam(defaultValue = "1") int currentPage, @RequestParam(defaultValue = "") String searchWord) {
		Emp loginProfessor = getLoginProfessor(session);
		
		int rowPerPage = 10;       // 한 페이지에 표시할 강의 수
	    int pageBlock = 10;        // 한 블록에 표시할 페이지 수

	    // 강의 리스트
	    List<Course> courseList = professorService.courseListByPage(loginProfessor.getEmpNo(), currentPage, searchWord);

	    // 전체 강의 수
	    int totalCount = professorService.getCourseCount(loginProfessor.getEmpNo(), searchWord);

	    // PageInfo 생성
	    PageInfo pageInfo = getPageInfo(totalCount, currentPage, rowPerPage, pageBlock);

	    // Mustache에서 편하게 쓰기 위해 pageList에 isCurrent 표시
	    List<Map<String,Object>> pageListWithCurrent = new ArrayList<>();
	    for(int p : pageInfo.getPageList()) {
	        Map<String,Object> m = new HashMap<>();
	        m.put("page", p);
	        m.put("isCurrent", p == pageInfo.getCurrentPage());
	        pageListWithCurrent.add(m);
	    }
	    log.debug("courseList : " + courseList);

	    model.addAttribute("courseList", courseList);
	    model.addAttribute("searchWord", searchWord);
	    model.addAttribute("pageInfo", pageInfo);
	    model.addAttribute("pageList", pageListWithCurrent);
		
		return "/professor/courseList";
	}
	
	// 교수 정보 수정 폼	
	@GetMapping("/professor/modifyProfessorInfo")
	public String modifyProfessorInfo(HttpSession session, Model model) {
		Emp loginProfessor = getLoginProfessor(session);

        Emp e = professorService.professorInfo(loginProfessor.getEmpNo());
        normalizeEmpFields(e);

        model.addAttribute("e", e);
	    
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
        normalizeEmpFields(e);

        model.addAttribute("e", e);
		
		return "professor/professorInfo";
	}
	
	// 교수 홈
	@GetMapping("/professor/professorHome")
	public String professorHome(HttpSession session) {
		Emp loginProfessor = getLoginProfessor(session);
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
            if (!allowedExtensions.contains(ext)) {
                log.warn("허용되지 않은 파일 형식: {}", file.getOriginalFilename());
                return; // 기존 파일 유지
            }

            // UUID로 새 파일명 생성
            String newFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(uploadDir, newFileName);
            dest.getParentFile().mkdirs();

            try {
                file.transferTo(dest);

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

    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return (idx != -1) ? filename.substring(idx + 1) : "";
    }
}

