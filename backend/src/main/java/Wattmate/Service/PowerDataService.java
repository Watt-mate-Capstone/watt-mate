package Wattmate.Service;

import Wattmate.Entity.PowerData;
import Wattmate.Repository.PowerDataRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PowerDataService {

    private final PowerDataRepository powerDataRepository;

    private final String FASTAPI_URL = "http://43.201.202.195:8000/predict";

    public PowerDataService(PowerDataRepository powerDataRepository) {
        this.powerDataRepository = powerDataRepository;
    }

    @Transactional
    public Map<String, Object> analyzeAndSave(Integer userId, MultipartFile file) throws Exception {
        System.out.println("==== [Service] AI 분석 및 저장 프로세스 시작 (UserId: " + userId + ") ====");

        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(tempFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        System.out.println("[Service] FastAPI 서버로 파일 전송 중...");

        Map<String, Object> aiResult = restTemplate.postForObject(
                FASTAPI_URL,
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (aiResult == null || aiResult.get("hourlyHistory") == null) {
            throw new RuntimeException("AI 서버로부터 응답을 받지 못했습니다.");
        }

        System.out.println("[Service] AI 서버 분석 응답 수신 성공");

        powerDataRepository.deleteByUserId(userId);
        System.out.println("[Service] 기존 데이터 삭제 완료 (UserId: " + userId + ")");

        List<PowerData> saveList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

        List<Map<String, Object>> history = (List<Map<String, Object>>) aiResult.get("hourlyHistory");

        for (Map<String, Object> h : history) {
            PowerData pd = new PowerData();

            pd.setUserId(userId);

            String ts = (String) h.get("timestamp");
            pd.setRecordedAt(LocalDateTime.parse(ts, formatter));

            pd.setRealUsageKwh(((Number) h.get("usage")).floatValue());
            pd.setPredUsageKwh(0f);

            saveList.add(pd);
        }

        List<Number> preds = (List<Number>) aiResult.get("next24hPred");

        LocalDateTime nextTime = LocalDateTime.of(2026, 5, 1, 1, 0, 0);

        for (int i = 0; i < preds.size(); i++) {
            PowerData pd = new PowerData();

            pd.setUserId(userId);

            pd.setRecordedAt(nextTime.plusHours(i));

            pd.setRealUsageKwh(0f);
            pd.setPredUsageKwh(preds.get(i).floatValue());

            saveList.add(pd);
        }

        powerDataRepository.saveAll(saveList);

        System.out.println("[Service] 총 " + saveList.size() + "건의 전력 데이터(실측 + 5월 전체예측) 저장 완료");

        tempFile.delete();

        return aiResult;
    }
}