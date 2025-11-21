package com.example.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lms.dto.*;
import com.example.lms.mapper.PublicMapper;

@Service
@Transactional
public class PublicService {
	@Autowired
	PublicMapper publicMapper;
	public Student loginStudent(LoginUser lu) {
		return publicMapper.selectLoginStudent(lu);
	}
	public Emp loginProfessor(LoginUser lu) {
		return publicMapper.selectLoginProfessor(lu);
	}
	public Emp loginEmp(LoginUser lu) {
		return publicMapper.selectLoginEmp(lu);
	}
}
