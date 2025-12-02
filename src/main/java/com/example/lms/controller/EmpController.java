package com.example.lms.controller;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
import com.example.lms.service.ProfessorService;
import com.example.lms.service.PublicService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class EmpController {
	@Autowired EmpService empService;
	@Autowired PublicService publicService;
	private final String uploadDir = "C:/lms/uploads";
	@InitBinder("modifyProfessor")
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				setValue((text == null || text.trim().isEmpty()) ? null : text.trim());
			}
		});
	}

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
		
		// 서비스 호출
		empService.insertNotice(n, files, uploadDir);
		
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
		empService.modifyNotice(notice, files, deleteFiles, uploadDir);
		return "redirect:/public/noticeOne?noticeNo=" + notice.getNoticeNo();
	}
	
	// 공지사항 삭제
	@PostMapping("/emp/deleteNotice")
	public String deleteNotice(int noticeNo, HttpServletRequest request) {
		empService.deleteNotice(noticeNo, uploadDir);
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
		int rowPerPage = empService.getRowPerPage();
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
		model.addAttribute("pageList", pageList);
		model.addAttribute("prePage", prePage);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("searchName", searchName);
		model.addAttribute("searchDept", searchDept);
		return "/emp/professorList";
	}
	
	// 교수 정보
	@GetMapping("/emp/professorInfo")
	public String professorInfo(Model model, int prfNo) {
		ProfessorInfo pi = empService.getProfessorInfo(prfNo);
		System.out.println("조회된 교수번호 = " + pi.getPrfNo());
		model.addAttribute("professor", pi);
		return "/emp/professorInfo";
	}
	
	// 교수 정보 수정
	@GetMapping("/emp/modifyProfessor")
	public String modifyProfessor(Model model, int prfNo) {
		ProfessorInfo pi = empService.getProfessorInfo(prfNo);
		List<Dept> deptList = empService.getDeptList();
		for(Dept d : deptList) {
			if(pi.getDeptName() != null && (pi.getDeptName()).equals(d.getDeptName())) {
				d.setSelected(true);
			}
		}
		
		if(pi.getPrfEmail() != null && pi.getPrfEmail().contains("@")) {
			String prefix = pi.getPrfEmail().split("@")[0];
			pi.setPrfEmail(prefix);	//prefix만 넣음
		}
		
		model.addAttribute("professor", pi);
		model.addAttribute("deptList", deptList);
		return "/emp/modifyProfessor";
	}
	@PostMapping("/emp/modifyProfessor")
	public String modifyProfessor(ProfessorInfo pi, @RequestParam(value = "prfImgFile", required = false)  MultipartFile file) throws Exception {
		File dir = new File(uploadDir);
		if(!dir.exists()) dir.mkdir();
		
		String newFileName = null;
		
		if(file != null && !file.isEmpty()) {
			// 새 파일명 생성
			String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
			newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
			
			// 저장
			File saveFile = new File(uploadDir, newFileName);
			file.transferTo(saveFile);
			
			// 기존 이미지 삭제
			if(pi.getPrfImg() != null && !pi.getPrfImg().equals("")) {
				File old = new File(uploadDir, pi.getPrfImg());
				if(old.exists()) old.delete();
			}
			
			// DB에 저장할 새 파일명 set
			pi.setPrfImg(newFileName);
		}
		
		// 사진변경 안했을 시 기존 prfImg 유지
		if(newFileName == null) {
			pi.setPrfImg(pi.getPrfImg());
		}
		pi.setPrfEmail(pi.getPrfEmail() + "@kru.com");
		empService.updateProfessor(pi);
		return "redirect:/emp/professorInfo?prfNo=" + pi.getPrfNo();
	}
	
	// 교수정보 삭제
	@GetMapping("/emp/deleteProfessor")
	public String delteProfessor(int prfNo) {
		empService.deleteProfessor(prfNo);
		return "redirect:/emp/professorList";
	}
}
