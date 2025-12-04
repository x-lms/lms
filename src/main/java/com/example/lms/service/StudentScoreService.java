package com.example.lms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.dto.Score;
import com.example.lms.mapper.StudentScoreMapper;

@Service
public class StudentScoreService {

    @Autowired
    private StudentScoreMapper scoreMapper;

    // 학생 성적 조회 
    public List<Score> getScoreList(int studentNo) {
        return scoreMapper.selectScoreList(studentNo);
    }
}
