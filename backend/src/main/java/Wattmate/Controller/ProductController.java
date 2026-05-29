package Wattmate.Controller;

import Wattmate.Entity.LogType;
import Wattmate.Entity.Product;
import Wattmate.Entity.User;
import Wattmate.Entity.PointLog;
import Wattmate.Repository.ProductRepository;
import Wattmate.Repository.UserRepository;
import Wattmate.Repository.PointLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PointLogRepository pointLogRepository;

    public ProductController(ProductRepository productRepository, UserRepository userRepository, PointLogRepository pointLogRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.pointLogRepository = pointLogRepository;
    }

    // 1. 상품 목록 조회 API
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // 2. [강화 버전] 상품 구매 및 포인트 차감 API
    @PostMapping("/purchase")
    @Transactional
    public ResponseEntity<?> purchaseProduct(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("userId") == null || request.get("productId") == null) {
                return ResponseEntity.badRequest().body("🚨 userId 또는 productId가 요청 데이터에서 누락되었습니다.");
            }

            // 🌟 [핵심 패치] Jackson의 Long/Integer 타입 변환 꼬임 에러를 원천 차단하는 가장 안전한 파싱법
            Integer userId = Integer.valueOf(request.get("userId").toString());
            Integer productId = Integer.valueOf(request.get("productId").toString());

            // 유저 및 상품 검증
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + productId));

            if (product.getStock() <= 0) {
                return ResponseEntity.badRequest().body("⚠️ 선택하신 상품은 품절되었습니다.");
            }

            if (user.getCurrentPoint() < product.getPricePoint()) {
                return ResponseEntity.badRequest().body("⚠️ 보유하신 포인트(WP)가 부족합니다. 현재 포인트: " + user.getCurrentPoint());
            }

            // 🪙 회원 지갑 포인트 차감 및 저장
            user.setCurrentPoint(user.getCurrentPoint() - product.getPricePoint());
            userRepository.save(user);

            // 🎁 상품 재고 차감 및 저장
            product.setStock(product.getStock() - 1);
            productRepository.save(product);

            // 📑 영수증 내역(PointLog) 적재 파트 (엔티티 필드 불일치 방어용 세부 try-catch 장착)
            try {
                PointLog log = new PointLog();
                log.setUserId(user.getUserId()); // 만약 연관관계 매핑이라면 log.setUser(user); 등으로 교체해야 할 수 있습니다.
                log.setAmount(product.getPricePoint());
                log.setLogType(LogType.valueOf("SPEND")); // 만약 ENUM 객체 요구 시 LogType.SPEND 형태로 맞춰주세요.
                log.setDescription(product.getProductName() + " 구매");
                log.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

                pointLogRepository.save(log);
            } catch (Exception logEx) {
                logEx.printStackTrace();
                return ResponseEntity.status(500).body("❌ PointLog 영수증 테이블 저장 중 오류 발생: " + logEx.getMessage() + " (자바 엔티티 필드 구성을 확인하세요)");
            }

            return ResponseEntity.ok().body("🎁 상품 교환 성공 완료!");

        } catch (Exception e) {
            // 인텔리제이(IntelliJ) 콘솔 창에 정확한 빨간색 에러 원인 StackTrace를 강제로 출력시킵니다.
            e.printStackTrace();
            return ResponseEntity.status(500).body("🚨 서버 내부 에러 발생 원인: " + e.getMessage());
        }
    }
}