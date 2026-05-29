package Wattmate.Controller;

import Wattmate.Entity.LogType;
import Wattmate.Entity.PointLog;
import Wattmate.Entity.Product;
import Wattmate.Entity.User;
import Wattmate.Repository.PointLogRepository;
import Wattmate.Repository.ProductRepository;
import Wattmate.Repository.UserRepository;
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

    public ProductController(
            ProductRepository productRepository,
            UserRepository userRepository,
            PointLogRepository pointLogRepository
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.pointLogRepository = pointLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping("/purchase")
    @Transactional
    public ResponseEntity<?> purchaseProduct(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("userId") == null || request.get("productId") == null) {
                return ResponseEntity.badRequest().body("🚨 userId 또는 productId가 요청 데이터에서 누락되었습니다.");
            }

            Integer userId = Integer.valueOf(request.get("userId").toString());
            Integer productId = Integer.valueOf(request.get("productId").toString());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + productId));

            if (product.getStock() == null || product.getStock() <= 0) {
                return ResponseEntity.badRequest().body("⚠️ 선택하신 상품은 품절되었습니다.");
            }

            if (user.getCurrentPoint() == null || user.getCurrentPoint() < product.getPricePoint()) {
                return ResponseEntity.badRequest().body("⚠️ 보유하신 포인트(WP)가 부족합니다. 현재 포인트: " + user.getCurrentPoint());
            }

            user.setCurrentPoint(user.getCurrentPoint() - product.getPricePoint());
            userRepository.save(user);

            product.setStock(product.getStock() - 1);
            productRepository.save(product);

            PointLog log = new PointLog();
            log.setUser(user);
            log.setAmount(product.getPricePoint());
            log.setLogType(LogType.SPEND);
            log.setDescription(product.getProductName() + " 구매");
            log.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            pointLogRepository.save(log);

            return ResponseEntity.ok("🎁 상품 교환 성공 완료!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("🚨 서버 내부 에러 발생 원인: " + e.getMessage());
        }
    }
}