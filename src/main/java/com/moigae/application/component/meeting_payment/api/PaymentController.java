package com.moigae.application.component.meeting_payment.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moigae.application.component.meeting_payment.application.MeetingPaymentService;
import com.moigae.application.component.meeting_payment.repository.MeetingPaymentRepository;
import com.moigae.application.component.user.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final MeetingPaymentService meetingPaymentService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String SECRET_KEY = "test_sk_BE92LAa5PVbkGazRNzZr7YmpXyJj";
    private final MeetingPaymentRepository meetingPaymentRepository;

    @PostConstruct
    private void init() {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
            }
        });
    }

    @GetMapping("/meetings/{meetingId}/success")
    @ResponseBody
    public String confirmPayment(Model model,
                                 @PathVariable String meetingId,
                                 @AuthenticationPrincipal CustomUser customUser,
                                 @RequestParam String paymentKey,
                                 @RequestParam String orderId,
                                 @RequestParam Long amount,
                                 HttpServletResponse response) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("orderId", orderId);
        payloadMap.put("amount", String.valueOf(amount));

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(payloadMap), headers);
        ResponseEntity<JsonNode> responseEntity = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/" + paymentKey, request, JsonNode.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonNode successNode = responseEntity.getBody();
            model.addAttribute("orderId", successNode.get("orderId").asText());
            String secret = successNode.get("secret").asText(); // 가상계좌의 경우 입금 callback 검증을 위해서 secret을 저장하기를 권장
            /**
             * 성공 상태 코드를 전달 받은 경우에만 DB에 저장. 즉, 결제 후 저장 로직
             */
            meetingPaymentService.meetingPaymentCreate(orderId, amount, customUser, meetingId);
            response.sendRedirect("applications");
            return "meetings/success";
        } else {
            JsonNode failNode = responseEntity.getBody();
            model.addAttribute("message", failNode.get("message").asText());
            model.addAttribute("code", failNode.get("code").asText());
            response.sendRedirect("applications");
            return "meetings/fail";
        }
    }

    @GetMapping("/meetings/{meetingId}/fail")
    @ResponseBody
    public String failPayment(Model model,
                              @RequestParam String message,
                              @RequestParam String code) {
        model.addAttribute("message", message);
        model.addAttribute("code", code);
        return "meetings/fail";
    }

    //유료 모임 - 중복 결제를 방지하기 위한 컨트롤러
    @PostMapping("/check-orderId")
    public ResponseEntity<?> checkOrderId(@RequestBody Map<String, Object> payload) {
        String orderId = (String) payload.get("orderId");
        if (orderId == null) {
            return ResponseEntity.badRequest().body("orderId가 존재하지 않습니다.");
        }
        boolean exists = meetingPaymentService.checkOrderId(orderId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/meetings/cancel")
    @ResponseBody
    @Transactional
    public Map<String, Boolean> findPassWord(
            @RequestBody Map<String, String> req) {

        String paymentId = req.get("paymentId");
        Long paymentIdLong = Long.parseLong(paymentId);
        meetingPaymentRepository.deleteById(paymentIdLong);

        Map<String, Boolean> response = new HashMap<>();
        response.put("statusSym", true);

        return response;
    }
}