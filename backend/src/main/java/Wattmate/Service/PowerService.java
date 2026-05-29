package Wattmate.Service;

import Wattmate.DTO.PowerAnalysisResponse;
import Wattmate.Entity.PowerData;
import Wattmate.Repository.PowerDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class    PowerService {

    // RestTemplate은 빈으로 등록해서 사용하는 것이 좋지만, 우선 직접 생성하여 사용합니다.
    private final RestTemplate restTemplate = new RestTemplate();

    public PowerAnalysisResponse analyzeAndSave(MultipartFile file) {
        // 1. FastAPI 서버 주소 설정
        // 같은 서버(EC2) 내에서 돌아가고 있다면 127.0.0.1이 가장 정확합니다.
        // 만약 Docker 환경이라면 'http://fastapi-container:8000/predict' 식으로 서비스명을 써야 할 수도 있습니다.
        String fastApiUrl = "http://127.0.0.1:8000/predict";

        try {
            // 2. FastAPI로 전송할 멀티파트 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 3. 파일 데이터를 바디에 담기
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 4. FastAPI 호출 로그 출력
            System.out.println(">>> [AI 분석 요청 시작] URL: " + fastApiUrl);

            ResponseEntity<PowerAnalysisResponse> response = restTemplate.postForEntity(
                    fastApiUrl,
                    requestEntity,
                    PowerAnalysisResponse.class
            );

            // 5. 결과 반환 및 저장
            PowerAnalysisResponse result = response.getBody();
            if (result != null) {
                System.out.println(">>> [AI 분석 성공] 데이터를 DB에 저장합니다.");
                saveToDatabase(result);
            }

            return result;

        } catch (Exception e) {
            // 6. 에러 발생 시 상세 로그 출력 (404, Connection Refused 등의 원인 파악용)
            System.err.println(">>> [AI 서버 통신 에러 발생] 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 분석 서비스 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void saveToDatabase(PowerAnalysisResponse result) {
        // TODO: 실제 DB 저장 로직 구현 (Repository 호출 등)
        // 예: powerDataRepository.save(result.toEntity());
        System.out.println(">>> DB 저장 완료: " + result.toString());
    }
}