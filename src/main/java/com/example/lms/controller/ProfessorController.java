package com.example.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.lms.dto.Emp;
import com.example.lms.service.ProfessorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ProfessorController {
	@Autowired
	ProfessorService professorService;
	
		
	@GetMapping("/professor/modifyProfessorInfo")
	public String modifyProfessorInfo(Model model, Integer empNo) {
		Emp e = professorService.professorInfo(empNo);
		log.debug("empNo: " + empNo);
		log.debug("e: " + e);
		model.addAttribute("e", e);
		if (e.getEmpBirth() == null) e.setEmpBirth("");
	    if (e.getEmpPhone() == null) e.setEmpPhone("");
	    if (e.getEmpImg() == null) e.setEmpImg("");
	    
		return "professor/modifyProfessorInfo";
	}
	
	@PostMapping("/professor/modifyProfessorInfo")
	public String modifyProfessorInfo(Emp e, Integer empNo ) {
		log.debug("empNo: " + empNo);
		professorService.updateProfessorInfo(e);
		return "redirect:/professor/professorInfo";
	}
	
	
	@GetMapping("/professor/professorInfo")
	public String professorInfo(Model model, Integer empNo) {
		Emp e = professorService.professorInfo(empNo);
		log.debug("e: "+e);
		
		model.addAttribute("e", e);
		// null이면 공백 처리
	    if (e.getEmpBirth() == null) e.setEmpBirth("");
	    if (e.getEmpPhone() == null) e.setEmpPhone("");
	    if (e.getEmpImg() == null) e.setEmpImg("");
		
		return "professor/professorInfo";
	}
	
	@GetMapping("/professor/professorIndex")
	public String navProfessor() {
		return "professor/professorIndex";
	}
}
