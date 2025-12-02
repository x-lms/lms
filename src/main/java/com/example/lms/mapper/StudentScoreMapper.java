package com.example.lms.mapper;

import java.util.List;
import com.example.lms.dto.Score;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentScoreMapper {
    List<Score> selectScoreList(int studentNo);
    Score selectScoreOne(int scoreNo);
}
