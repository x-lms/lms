package com.example.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.lms.dto.*;
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
	public String noticeList() {
		
		return "public/noticeList";
	}

}
