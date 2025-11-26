package com.example.lms.dto;

import lombok.Data;

@Data
public class TimetableCell {
    private int period;    // 교시 1~9
    private String mon = "";
    private String tue = "";
    private String wed = "";
    private String thu = "";
    private String fri = "";
}
