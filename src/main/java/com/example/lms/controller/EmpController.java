package com.example.lms.controller;

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
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.dto.Dept;
import com.example.lms.dto.Emp;
import com.example.lms.dto.Notice;
import com.example.lms.dto.NoticeFile;
import com.example.lms.dto.ProfessorInfo;
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
	
	// 공지사항 추가
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
	@PostMapping("/emp/deleteNotice")
	public String deleteNotice(int noticeNo, HttpServletRequest request) {
		String uploadPath = request.getServletContext().getRealPath("/upload");
		empService.deleteNotice(noticeNo, uploadPath);
		return "redirect:/public/noticeList";
	}
	// 교수 추가
	@GetMapping("/emp/addProfessor")
	public String addProfessor(Model model) {
		List<Dept> deptList = empService.getDeptList();
		model.addAttribute("deptList", deptList);
		return "/emp/addProfessor";
	}
	@PostMapping("/emp/addProfessor")
	public String addProfessor(Emp e) {
		if(e.getDeptNo().equals(0)) e.setDeptNo(null);
		
		empService.addProfessor(e);
		return "redirect:/emp/professorList";
	}
	
	// 교수 리스트
	@GetMapping("/emp/professorList")
	public String professorList(Model model, @RequestParam(defaultValue = "1") int currentPage
								, @RequestParam(defaultValue = "") String searchName, @RequestParam(defaultValue = "") String searchDept) {
		List<Dept> deptList = empService.getDeptList();
		for(Dept d : deptList) {
			if(searchDept != null && searchDept.equals(String.valueOf(d.getDeptNo()))) {
				d.setSelected(true);
			}
		}
		
		List<ProfessorInfo> prfList = empService.getProfessorList(currentPage, searchName, searchDept);
		
		// 페이징 계산
		int rowPerPage = publicService.getRowPerPage();
		int totalCount = empService.countPrf(searchName, searchDept);
		int totalPage = (int) Math.ceil((double) totalCount / rowPerPage);
		
		// 페이지 블록 단위 설정
		int blockSize = 5;
		int currentBolock = (currentPage - 1) / blockSize;
		int startPage = currentBolock * blockSize + 1;
		int endPage = Math.min(startPage + blockSize - 1, totalPage);
		
		// 페이지 버튼용 리스트
		List<Map<String, Object>> pageList = new ArrayList<>();
		for( int i = startPage; i <= endPage; i++) {
			Map<String, Object> pageMap = new HashMap<>();
			pageMap.put("pageNo", i);
			pageMap.put("active", i == currentPage);
			pageList.add(pageMap);
		}
		
		// 이전/다음 블록 버튼
		Integer prePage = startPage > 1 ? startPage - 1 : null;
		Integer nextPage = endPage < totalPage ? endPage + 1 : null;
		
		model.addAttribute("deptList", deptList);
		model.addAttribute("prfList", prfList);
		model.addAttribute("prePage", prePage);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("searchName", searchName);
		model.addAttribute("searchDept", searchDept);
		return "/emp/professorList";
	}
}
