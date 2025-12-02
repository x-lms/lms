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

    // 총점 및 등급 계산 메서드
    private void calculateTotalAndGrade(Score score) {
        double total = score.getScoreAtt() + score.getScoreProject() 
                       + score.getScoreMid() + score.getScoreFin();
        score.setScoreTotal(total);

        // 출석 기준 F 처리
        if (score.getScoreAtt() <= 0) { 
            score.setScoreGrade("F");
        } else if (total >= 95) {
            score.setScoreGrade("A+");
        } else if (total >= 90) {
            score.setScoreGrade("A0");
        } else if (total >= 85) {
            score.setScoreGrade("B+");
        } else if (total >= 80) {
            score.setScoreGrade("B0");
        } else if (total >= 75) {
            score.setScoreGrade("C+");
        } else if (total >= 70) {
            score.setScoreGrade("C0");
        } else if (total >= 60) {
            score.setScoreGrade("D0");
        } else {
            score.setScoreGrade("F");
        }
    }

    public List<Score> getScoreList(int studentNo) {
        List<Score> list = scoreMapper.selectScoreList(studentNo);
        for (Score s : list) {
            calculateTotalAndGrade(s);
        }
        return list;
    }

   
}
