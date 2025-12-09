package com.example.lms.controller;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.lms.dto.*;
import com.example.lms.service.PublicService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
public class PublicController {
	@Autowired
	PublicService publicService;
	private final String uploadDir = "C:/lms/uploads";
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
	
	// 아이디 찾기
	@GetMapping("/findId")
	public String findId() {
		return "/public/findId";
	}
	@PostMapping("/findId")
	@ResponseBody
	public Map<String, Object> findId(@RequestParam Map<String, String> paramMap) {
		Map<String, Object> result = new HashMap<>();
		
		String type = paramMap.get("type");
		String foundId = null;
		
		if(type.equals("student")) {
			Student s = new Student();
			s.setStudentName(paramMap.get("studentName"));
			s.setStudentEmail(paramMap.get("studentEmail"));
			s.setStudentBirth(paramMap.get("studentBirth"));
			s.setStudentPhone(paramMap.get("studentPhone"));
			
			foundId = publicService.findStudentId(s);
		}
		if(type.equals("emp")) {
			Emp e = new Emp();
			e.setEmpName(paramMap.get("empName"));
			e.setEmpBirth(paramMap.get("empBirth"));
			e.setEmpPhone(paramMap.get("empPhone"));
			
			foundId = publicService.findEmpId(e);
		}
		
		if (foundId != null) {
			result.put("success", true);
			result.put("id", foundId);
		} else {
			result.put("success", false);
		}
		return result;
	}
	
	// 비밀번호 찾기
	@GetMapping("/findPw")
	public String findPw() {
		return "/public/findPw";
	}
	
	@PostMapping("/findPw")
	@ResponseBody
	public Map<String, Object> findPw(HttpServletRequest request, @RequestParam Map<String, String> param) {
		Map<String, Object> result = new HashMap<>();
		
		String type = param.get("type");
		
		int userNo = 0;
		if(type.equals("student")) {
			Student s = new Student();
			s.setStudentNo(Integer.parseInt(param.get("studentNo")));
			s.setStudentName(param.get("studentName"));
			s.setStudentEmail(param.get("studentEmail"));
			
			userNo = publicService.findStudentNo(s);
		}
		if(type.equals("emp")) {
			Emp e = new Emp();
			e.setEmpName(param.get("empName"));
			e.setEmpEmail(param.get("empEmail"));
			e.setEmpBirth(param.get("empBirth"));
			e.setEmpPhone(param.get("empPhone"));
			
			userNo = publicService.findEmpNo(e);
		}
		log.debug("userNo: " + userNo);
		if(userNo == 0) {
			result.put("success", false);
			return result;
		}
		
		String token = UUID.randomUUID().toString();
		request.getSession().setAttribute("pwResetToken", token);
		request.getSession().setAttribute("pwResetUserNo", userNo);
		request.getSession().setAttribute("pwResetUserType", type);
		result.put("success", true);
		result.put("redirectUrl", "/resetPw?token=" + token);
		
		return result;
	}
	
	@GetMapping("/resetPw")
	public String resetPw(Model model, HttpSession session, @RequestParam(required = false) String token) {
		String sessionToken = (String) session.getAttribute("pwResetToken");
		
		// 접근 차단
		if(sessionToken == null || !sessionToken.equals(token)) return "redirect:/findPw";
		
		return "public/resetPw";
	}
	@PostMapping("/resetPw")
	@ResponseBody
	public Map<String, Object> resetPwDo(HttpSession session, @RequestParam("newPw") String newPw) {
		Map<String, Object> result = new HashMap<>();
		Integer userNo = (Integer) session.getAttribute("pwResetUserNo");
		String userType = (String) session.getAttribute("pwResetUserType");
		log.debug("userNo, userType: " + userNo + ", " + userType);
		if(userNo == null) {
			result.put("success", false);
			return result;
		}
		if(userType.equals("student")) {
			Student s = new Student();
			s.setStudentNo(userNo);
			s.setStudentPw(newPw);
			
			publicService.changeStudentPw(s);
			result.put("success", true);
		}
		if(userType.equals("emp")) {
			Emp e = new Emp();
			e.setEmpNo(userNo);
			e.setEmpPw(newPw);
			
			publicService.changeEmpPw(e);
			result.put("success", true);
		}
		
		session.removeAttribute("pwResetToken");
		session.removeAttribute("pwResetUserNo");
		session.removeAttribute("pwResetUserType");
		
		return result;
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
	
	// 공지사항 상세보기
	@GetMapping("/public/noticeOne")
	public String noticeOne(Model model, int noticeNo) {
		Notice noticeOne = publicService.getNoticeOne(noticeNo);
		List<NoticeFile> fileList = publicService.getNoticeFileList(noticeNo);
		
		model.addAttribute("noticeNo", noticeNo);
		model.addAttribute("noticeOne", noticeOne);
		model.addAttribute("fileList", fileList);
		return "/public/noticeOne";
	}
	@GetMapping("/public/downloadNoticeFile")
	public void downloadNoticeFile(int fileNo, HttpServletResponse response, HttpServletRequest request) throws Exception {
		NoticeFile nf = publicService.getNoticeFile(fileNo);
		
		if(nf == null) {
			throw new RuntimeException("파일 정보가 존재하지 않습니다.");
		}
		
		// 파일이 실제 저장된 경로
		String filePath = uploadDir + "/" + nf.getFileName();
		File file = new File(filePath);
		
		if(!file.exists()) {
			throw new RuntimeException("파일이 서버에 존재하지 않습니다.");
		}
		
		// 헤더 설정
		response.setContentType(nf.getFileType());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(nf.getOriginName(), "UTF-8") + "\"");
	    response.setContentLengthLong(file.length());
	    
	    // 파일 스트림 전송
	    FileInputStream fis = new FileInputStream(file);
	    ServletOutputStream sos = response.getOutputStream();

	    FileCopyUtils.copy(fis, sos);

	    fis.close();
	    sos.close();
	}
	// 스케줄 홈
	@GetMapping("/public/schedule")
	public String schedule() {
		return "/public/schedule";
	}
	// 스케줄 상세보기
	@GetMapping("/public/scheduleOne")
	public String scheduleInfo(Model model, int scheduleNo) {
		Schedule schedule = publicService.getSchedule(scheduleNo);
		Emp writer = publicService.selectEmp(schedule.getScheduleWriter());
		model.addAttribute("scheduleNo", scheduleNo);
		model.addAttribute("writer", writer.getEmpName());
		model.addAttribute("schedule", schedule);
		return "/public/scheduleOne";
	}
	// 스케줄 리스트
	@GetMapping("/public/scheduleList")
	@ResponseBody
	public List<Map<String, Object>> scheduleList() {
		List<Schedule> list = publicService.getScheduleList();
	    List<Map<String, Object>> result = new ArrayList<>();
	    log.debug("일정 개수: " + list.size());
	    for (Schedule s : list) {
	        Map<String, Object> map = new HashMap<>();
	        map.put("id", s.getScheduleNo());
	        map.put("title", s.getScheduleTitle());
	        map.put("start", s.getScheduleStartDate().toString());
	        LocalDate endPlusOne = s.getScheduleEndDate().plusDays(1);
	        map.put("end", endPlusOne.toString());
	        result.add(map);
	    }
		return result;
	}
}
