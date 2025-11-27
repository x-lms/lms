package com.example.lms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.dto.Emp;
import com.example.lms.dto.Notice;
import com.example.lms.dto.NoticeFile;
import com.example.lms.service.EmpService;
import com.example.lms.service.PublicService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class EmpController {
	@Autowired EmpService empService;
	@Autowired PublicService publicService;
	
	@GetMapping("/emp/empHome")
	public String empHome(@SessionAttribute("loginEmp") Emp loginEmp) {
		return "/emp/empHome";
	}
	
	// 공지사항 핫제
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
	
	// 공지사항 수정
	@GetMapping("/emp/modifyNotice")
	public String modifyNotice(Model model, int noticeNo) {
		Notice noticeOne = publicService.getNoticeOne(noticeNo);
		List<NoticeFile> fileList = publicService.getNoticeFileList(noticeNo);
		
		model.addAttribute("noticeNo", noticeNo);
		model.addAttribute("noticeOne", noticeOne);
		model.addAttribute("fileList", fileList);
		return "/emp/modifyNotice";
	}
	@PostMapping("/emp/modifyNotice")
	public String modifyNotice(Notice notice, HttpServletRequest request,
								@RequestParam(value="attachments", required=false) MultipartFile[] files,
								@RequestParam(value="deleteFileNos", required=false) String deleteFiles) {
		String path = request.getServletContext().getRealPath("/upload");
		empService.modifyNotice(notice, files, deleteFiles, path);
		return "redirect:/public/noticeOne?noticeNo=" + notice.getNoticeNo();
	}
	
	// 공지사항 삭제
}
