package com.example.lms.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.lms.dto.Course;
import com.example.lms.dto.Emp;
import com.example.lms.service.ProfessorService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ProfessorController {
	@Autowired
	ProfessorService professorService;
	
	// 강의 상세보기
	@GetMapping("/professor/courseOne")
	public String courseOne(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
		        throw new RuntimeException("로그인 세션 없음");
		    }
		Course course = professorService.getCourseOne(courseNo);
		model.addAttribute("course", course);
		return "/professor/courseOne";
	}
	
	// 강의 등록
	@GetMapping("/professor/addCourse")
	public String addCourse(HttpSession session) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
		        throw new RuntimeException("로그인 세션 없음");
		    }
		return "/professor/addCourse";
	}
	
	// 강의 수정 폼
	@GetMapping("/professor/modifyCourse")
	public String courseUpdate(HttpSession session, Model model, int courseNo) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
			throw new RuntimeException("로그인 세션 없음");
		}
		Course c = professorService.getCourseOne(courseNo);
		model.addAttribute("c", c);
		
		return "/professor/modifyCourse";
	}
	
	// 강의 수정 액션
	@PostMapping("/professor/modifyCourse")
	public String courseUpdate(HttpSession session, Course course) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
			throw new RuntimeException("로그인 세션 없음");
		}

	    professorService.updateCourse(course);

	    return "redirect:/professor/courseOne?courseNo=" + course.getCourseNo();
	}
	
	// 강의 삭제
	@PostMapping("/professor/removeCourse")
	public String courseDelete(HttpSession session, int courseNo) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
			throw new RuntimeException("로그인 세션 없음");
		}

	    professorService.deleteCourse(courseNo);
	    return "redirect:/professor/courseList";
	}
	
	@PostMapping("/professor/addCourse")
	public String addCourse(HttpSession session, Course c) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
			throw new RuntimeException("로그인 세션 없음");
		}
		
		professorService.insertCourse(c);
		
		return "redirect:/professor/courseList";
	}
	
	// 강의리스트
	@GetMapping("/professor/courseList")
	public String courseList(HttpSession session, Model model, @RequestParam(defaultValue = "1") int currentPage, @RequestParam(defaultValue = "") String searchWord) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
		        throw new RuntimeException("로그인 세션 없음");
		    }
		int rowPerPage = 10;       // 한 페이지에 표시할 강의 수
	    int pageBlock = 10;        // 한 블록에 표시할 페이지 수

	    // 강의 리스트
	    List<Course> courseList = professorService.courseListByPage(loginProfessor.getEmpNo(), currentPage, searchWord);

	    // 전체 강의 수
	    int totalCount = professorService.getCourseCount(loginProfessor.getEmpNo(), searchWord);

	    // 마지막 페이지 계산
	    int lastPage = (int) Math.ceil((double) totalCount / rowPerPage);

	    // 페이지 블록 계산
	    int startPage = ((currentPage - 1) / pageBlock) * pageBlock + 1;
	    int endPage = startPage + pageBlock - 1;
	    if (endPage > lastPage) endPage = lastPage;

	    // 이전/다음 페이지
	    Integer prePage = (startPage > 1) ? startPage - 1 : null;
	    Integer nextPage = (endPage < lastPage) ? endPage + 1 : null;

	    // 페이지 번호 리스트
	    List<Map<String, Object>> pageList = new ArrayList<>();
	    for (int i = startPage; i <= endPage; i++) {
	        Map<String, Object> pageMap = new HashMap<>();
	        pageMap.put("page", i);
	        pageMap.put("isCurrent", i == currentPage);
	        pageList.add(pageMap);
	    }

	    log.debug("courseList : " + courseList);
	    
	    model.addAttribute("courseList", courseList);
	    model.addAttribute("searchWord", searchWord);
	    model.addAttribute("currentPage", currentPage);
	    model.addAttribute("prePage", prePage);
	    model.addAttribute("nextPage", nextPage);
	    model.addAttribute("lastPage", lastPage);
	    model.addAttribute("pageList", pageList);
		
		return "/professor/courseList";
	}
	
	// 교수 정보 폼	
	@GetMapping("/professor/modifyProfessorInfo")
	public String modifyProfessorInfo(HttpSession session, Model model) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
		        throw new RuntimeException("로그인 세션 없음");
		    }
		Emp e = professorService.professorInfo(loginProfessor.getEmpNo());
		log.debug("e: " + e);
		model.addAttribute("e", e);
		if (e.getEmpBirth() == null) e.setEmpBirth("");
	    if (e.getEmpPhone() == null) e.setEmpPhone("");
	    if (e.getEmpImg() == null) e.setEmpImg("");
	    
		return "professor/modifyProfessorInfo";
	}
	
	// 교수 정보 액션
	@PostMapping("/professor/modifyProfessorInfo")
	public String modifyProfessorInfo(HttpSession session, Emp e) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
		        throw new RuntimeException("로그인 세션 없음");
		    }
		e.setEmpNo(loginProfessor.getEmpNo());
		log.debug("empNo: " + e.getEmpNo());
		if (e.getEmpPw() == null || e.getEmpPw().isEmpty()) {
		       // DB에서 기존 비밀번호 가져오기
		       Emp e1 = professorService.professorInfo(loginProfessor.getEmpNo());
		       e.setEmpPw(e1.getEmpPw());
		   }
				
		professorService.updateProfessorInfo(e);
		return "redirect:/professor/professorInfo";
	}
	
	// 교수정보
	@GetMapping("/professor/professorInfo")
	public String professorInfo(HttpSession session, Model model) {
		Emp loginProfessor = (Emp) session.getAttribute("loginProfessor");
	    if (loginProfessor == null) {
		        throw new RuntimeException("로그인 세션 없음");
		    }
	    Emp e = professorService.professorInfo(loginProfessor.getEmpNo());
		log.debug("e: "+e);
		
		model.addAttribute("e", e);
		// null이면 공백 처리
	    if (e.getEmpBirth() == null) e.setEmpBirth("");
	    if (e.getEmpPhone() == null) e.setEmpPhone("");
	    if (e.getEmpImg() == null) e.setEmpImg("");
		
		return "professor/professorInfo";
	}
	
	// 교수 홈
	@GetMapping("/professor/professorHome")
	public String professorHome(HttpSession session) {
		Emp loginProfessor =  (Emp)session.getAttribute("loginProfessor");
		if (loginProfessor == null) {
	        throw new RuntimeException("로그인 세션 없음");
	    }
		return "professor/professorHome";
	}
}
