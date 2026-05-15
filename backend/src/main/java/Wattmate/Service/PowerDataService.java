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
    private final String FASTAPI_URL = "http://43.201.202.195:8000/predict";

    public PowerDataService(PowerDataRepository powerDataRepository) {
        this.powerDataRepository = powerDataRepository;
    }

    @Transactional
    public Map<String, Object> analyzeAndSave(Integer userId, MultipartFile file) throws Exception {
        System.out.println("==== [Service] AI 분석 및 저장 프로세스 시작 ====");

        // 1. 임시 파일 생성 (FastAPI에 전송하기 위함)
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        // 2. FastAPI 호출 준비
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(tempFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        System.out.println("[Service] FastAPI로 분석 요청 중...");
        Map<String, Object> aiResult = restTemplate.postForObject(FASTAPI_URL, new HttpEntity<>(body, headers), Map.class);
        System.out.println("[Service] AI 서버로부터 응답 수신 성공");

        // 3. 기존 데이터 삭제 (유저별 데이터 최신화)
        powerDataRepository.deleteByUserId(userId);
        System.out.println("[Service] 기존 데이터 삭제 완료 (UserId: " + userId + ")");

        // 4. 데이터 변환 및 저장 리스트 생성
        List<PowerData> saveList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // [과거 실측치 저장]
        List<Map<String, Object>> history = (List<Map<String, Object>>) aiResult.get("hourlyHistory");
        for (Map<String, Object> h : history) {
            PowerData pd = new PowerData();
            pd.setUserId(userId);
            pd.setRecordedAt(LocalDateTime.parse((String) h.get("timestamp"), formatter));
            pd.setRealUsageKwh(((Double) h.get("usage")).floatValue());
            pd.setPredUsageKwh(0f);
            saveList.add(pd);
        }

        // [미래 예측치 저장]
        List<Double> preds = (List<Double>) aiResult.get("next24hPred");
        LocalDateTime nextTime = LocalDateTime.now().withMinute(0).plusHours(1);
        for (int i = 0; i < 24; i++) {
            PowerData pd = new PowerData();
            pd.setUserId(userId);
            pd.setRecordedAt(nextTime.plusHours(i));
            pd.setRealUsageKwh(0f);
            pd.setPredUsageKwh(preds.get(i).floatValue());
            saveList.add(pd);
        }

        // 5. DB 일괄 저장
        powerDataRepository.saveAll(saveList);
        System.out.println("[Service] 총 " + saveList.size() + "건의 데이터 DB 저장 완료");

        tempFile.delete(); // 임시 파일 삭제
        return aiResult;
    }
}