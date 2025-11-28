package com.example.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.lms.dto.Dept;

@Mapper
public interface DeptMapper {

	List<Dept> getDeptList();

	Dept getDeptByNo(Integer deptNo);
	
}
