package com.example.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.dto.Emp;
import com.example.lms.dto.Notice;
import com.example.lms.service.EmpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class EmpController {
	@Autowired
	EmpService empService;
	@GetMapping("/emp/empHome")
	public String empHome(@SessionAttribute("loginEmp") Emp loginEmp) {
		return "/emp/empHome";
	}
	
	@GetMapping("/emp/addNotice")
	public String addNotice(Model model) {
		return "/emp/addNotice";
	}
	
	@PostMapping("/emp/addNotice")
	public String addNotice(@SessionAttribute("loginEmp") Emp loginEmp, HttpServletRequest request,
							Notice n, @RequestParam("noticeFile") MultipartFile[] files) {
		n.setEmpNo(loginEmp.getEmpNo());
		n.setNoticeWriter(loginEmp.getEmpName());
		
		// 업로드 폴더 절대경로
		String path = request.getServletContext().getRealPath("/upload");
		
		// 서비스 호출
		empService.insertNotice(n, files, path);
		
		return "redirect:/public/noticeList";
	}
}
