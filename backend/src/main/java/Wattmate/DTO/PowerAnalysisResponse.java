package Wattmate.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PowerAnalysisResponse {
    private List<Double> thisWeekAvg;  // 이번주 시간별 평균
    private List<Double> nextWeekPred; // 다음주 시간별 예측치
}
