package com.example.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.dto.Emp;
import com.example.lms.mapper.ProfessorMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProfessorService {
	@Autowired
	ProfessorMapper professorMapper;
	
	// 내 정보 수정 
	public int updateProfessorInfo(Emp e) {
		return professorMapper.updateProfessorInfo(e);
	}
	
	// 내 정보
	public Emp professorInfo(int empNo) {
		return professorMapper.professorInfo(empNo);
	}
	
}
