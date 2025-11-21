package com.example.lms.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Emp;


@Mapper
public interface ProfessorMapper {
	// 내 정보
	Emp professorInfo(int empNo);
	// 내 정보 수정
	int updateProfessorInfo(Emp e);
}
