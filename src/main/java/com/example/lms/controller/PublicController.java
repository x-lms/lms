package com.example.lms.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.example.lms.dto.LoginUser;
import com.example.lms.dto.Notice;
import com.example.lms.service.PublicService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
public class PublicController {
	@Autowired
	PublicService publicService;
	// 로그인 액션
	@GetMapping({"/", "/login"})
	public String login() {
		return "/public/login";
	}
	// 로그인 폼
	@PostMapping({"/", "/login"})
	public String login(HttpSession session, LoginUser lu) {
		log.debug(lu.toString());
		
		Object loginUser = switch(lu.getUserType()) {
			case "student" -> publicService.loginStudent(lu);
			case "professor" -> publicService.loginProfessor(lu);
			case "emp" -> publicService.loginEmp(lu);
			default -> null;
		};
		
		if(loginUser == null) {
			return "/public/login";
		}
		
		String userType = lu.getUserType().substring(0, 1).toUpperCase() + lu.getUserType().substring(1);
		String sessionKey = "login" + userType;
		log.debug(sessionKey);
		session.setAttribute(sessionKey, loginUser);
		
		return "redirect:/" + lu.getUserType() + "/" + lu.getUserType() + "Home";
	}
	
	// 로그아웃
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login"; // 리다이렉트
	}
	
	// 공지사항게시판
	@GetMapping("/public/noticeList")
	public String noticeList(Model model, @RequestParam(defaultValue = "1") int currentPage, @RequestParam(defaultValue = "") String searchWord) {
		List<Notice> noticeList = publicService.getNoticeList(currentPage, searchWord);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		
		List<Map<String, Object>> viewList = new ArrayList<>();
		LocalDate now = LocalDate.now();
		// new 태그 붙이기
		for (Notice n : noticeList) {
			Map<String, Object> m = new HashMap<>();
			m.put("notice", n);
			
			LocalDate created;
			try {
				created = LocalDate.parse(n.getCreatedate(), formatter);
			} catch (Exception e) {
				created = now.minusYears(10);
			}
			
			// 한달 이내 공지는 NEW
			boolean isNew = !created.isBefore(now.minusMonths(1));
			m.put("isNew", isNew);
			
			viewList.add(m);
		}
		
		// 페이징 계산
		int rowPerPage = publicService.getRowPerPage();
		int totalCount = publicService.countNotice(searchWord);
		int totalPage = (int) Math.ceil((double) totalCount / rowPerPage);
		
		// 페이지 블록 단위 설정
		int blockSize = 5;
		int currentBlock = (currentPage -1) / blockSize;
		int startPage = currentBlock * blockSize + 1;
		int endPage = Math.min(startPage + blockSize - 1, totalPage);
		
		// 페이지 버튼용 리스트
		List<Map<String, Object>> pageList = new ArrayList<>();
		for(int i = startPage; i <= endPage; i++) {
			Map<String, Object> pageMap = new HashMap<>();
			pageMap.put("pageNo", i);
			pageMap.put("active", i == currentPage);
			pageList.add(pageMap);
		}
		
		// 이전/다음 블록 버튼
	    Integer prePage = startPage > 1 ? startPage - 1 : null;
	    Integer nextPage = endPage < totalPage ? endPage + 1 : null;
	    
		model.addAttribute("noticeList", viewList);
		model.addAttribute("pageList", pageList);
		model.addAttribute("prePage", prePage);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("searchWord", searchWord);
		
		return "public/noticeList";
	}

}
