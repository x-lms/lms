package com.example.lms.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.*;
@Mapper
public interface PublicMapper {
	// 로그인
	Student selectLoginStudent(LoginUser loginUser);
	Emp selectLoginProfessor(LoginUser loginUser);
	Emp selectLoginEmp(LoginUser loginUser);
}
