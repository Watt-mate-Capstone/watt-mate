package Wattmate.Service;

import Wattmate.Entity.PowerData;
import Wattmate.Repository.PowerDataRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PowerDataService {

    private final PowerDataRepository powerDataRepository;
    // FastAPI 서버 주소 (실제 환경에 맞춰 확인 필요)
    private final String FASTAPI_URL = "http://43.201.202.195:8000/predict";

    public PowerDataService(PowerDataRepository powerDataRepository) {
        this.powerDataRepository = powerDataRepository;
    }

    @Transactional
    public Map<String, Object> analyzeAndSave(Integer userId, MultipartFile file) throws Exception {
        System.out.println("==== [Service] AI 분석 및 저장 프로세스 시작 (UserId: " + userId + ") ====");

        // 1. FastAPI로 전송할 임시 파일 생성
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        // 2. FastAPI 호출 (RestTemplate 이용)
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(tempFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        System.out.println("[Service] FastAPI 서버로 파일 전송 중...");
        Map<String, Object> aiResult = restTemplate.postForObject(FASTAPI_URL, new HttpEntity<>(body, headers), Map.class);

        if (aiResult == null || aiResult.get("hourlyHistory") == null) {
            throw new RuntimeException("AI 서버로부터 응답을 받지 못했습니다.");
        }
        System.out.println("[Service] AI 서버 분석 응답 수신 성공");

        // 3. 기존 데이터 삭제 (해당 유저의 데이터만 초기화하여 중복 방지)
        powerDataRepository.deleteByUserId(userId);
        System.out.println("[Service] 기존 데이터 삭제 완료 (UserId: " + userId + ")");

        // 4. 데이터 변환 및 일괄 저장을 위한 리스트 생성
        List<PowerData> saveList = new ArrayList<>();

        // 🌟 [핵심 수정] 마침표(.) 형식의 날짜를 읽기 위한 포맷터 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

        // [실측 데이터 가공] - 4개월치 전체 데이터
        List<Map<String, Object>> history = (List<Map<String, Object>>) aiResult.get("hourlyHistory");
        for (Map<String, Object> h : history) {
            PowerData pd = new PowerData();
            pd.setUserId(userId);

            // AI가 준 "2026.03.16 01:00" 형태의 문자열을 LocalDateTime으로 변환
            String ts = (String) h.get("timestamp");
            pd.setRecordedAt(LocalDateTime.parse(ts, formatter));

            pd.setRealUsageKwh(((Double) h.get("usage")).floatValue());
            pd.setPredUsageKwh(0f); // 과거 데이터이므로 예측값은 0
            saveList.add(pd);
        }

        // [예측 데이터 가공] - 미래 24시간
        List<Double> preds = (List<Double>) aiResult.get("next24hPred");
        // 현재 시점의 다음 정각부터 예측 데이터 시작
        LocalDateTime nextTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1);

        for (int i = 0; i < 24; i++) {
            PowerData pd = new PowerData();
            pd.setUserId(userId);
            pd.setRecordedAt(nextTime.plusHours(i));
            pd.setRealUsageKwh(0f); // 미래이므로 실측값은 0
            pd.setPredUsageKwh(preds.get(i).floatValue());
            saveList.add(pd);
        }

        // 5. DB 일괄 저장 (벌크 인서트 효과)
        powerDataRepository.saveAll(saveList);
        System.out.println("[Service] 총 " + saveList.size() + "건의 전력 데이터 저장 완료");

        // 임시 파일 삭제 및 결과 반환
        tempFile.delete();
        return aiResult;
    }
}