package com.example.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.lms.dto.Emp;
import com.example.lms.dto.LoginUser;
import com.example.lms.dto.Student;
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
		Student loginStudent = null;
		Emp loginProfessor = null;
		Emp loginEmp = null;
		log.debug(lu.toString());
		if(lu.getUserType().equals("student")) {
			loginStudent = publicService.loginStudent(lu);
			if(loginStudent == null) {	// 로그인 실패시 login.mustache로 다시 포워딩
				return "/public/login";
			}
			session.setAttribute("loginStudent", loginStudent);
		} else if(lu.getUserType().equals("professor")) {
			loginProfessor = publicService.loginProfessor(lu);
			if(loginProfessor == null) {	// 로그인 실패시 login.mustache로 다시 포워딩
				return "/public/login";
			}
			session.setAttribute("loginProfessor", loginProfessor);
		} else {
			loginEmp = publicService.loginEmp(lu);
			if(loginEmp == null) {	// 로그인 실패시 login.mustache로 다시 포워딩
				return "/public/login";
			}
			session.setAttribute("loginEmp", loginEmp);
		}
		
		return "redirect:/" + lu.getUserType() + "/" + lu.getUserType() + "Home";
	}
	
	// 로그아웃
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login"; // 리다이렉트
	}
	
	@GetMapping("/public/noticeList")
	public String noticeList() {
		return "public/noticeList";
	}

}
