package com.example.lms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.dto.Dept;
import com.example.lms.mapper.DeptMapper;

@Service
public class DeptService {
	@Autowired
	DeptMapper deptMapper;

	public List<Dept> getDeptList() {
		return deptMapper.getDeptList();
	}

	public Dept getDeptByNo(Integer deptNo) {
		return deptMapper.getDeptByNo(deptNo);
	}
}
