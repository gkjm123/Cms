package com.zerobase.cms.user.controller;

import com.zerobase.cms.user.application.SignUpApplication;
import com.zerobase.cms.user.domain.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignUpController {
    private final SignUpApplication signUpApplication;

    //고객 회원가입
    @PostMapping("/customer")
    public ResponseEntity<String> customerSignUp(@RequestBody SignUpForm form) {
        return ResponseEntity.ok(signUpApplication.customerSignUp(form));
    }

    //고객이 받은 인증 메일의 링크를 클릭해 접근
    @GetMapping("/customer/verify")
    public ResponseEntity<String> verifyCustomer(@RequestParam String email, @RequestParam String code) {
        signUpApplication.customerVerify(email, code);
        return ResponseEntity.ok("인증 완료");
    }

    //판매자 회원가입
    @PostMapping("/seller")
    public ResponseEntity<String> sellerSignUp(@RequestBody SignUpForm form) {
        return ResponseEntity.ok(signUpApplication.sellerSignUp(form));
    }

    //판매자가 받은 메일의 링크를 클릭해 접근
    @GetMapping("/seller/verify")
    public ResponseEntity<String> verifySeller(@RequestParam String email, @RequestParam String code) {
        signUpApplication.sellerVerify(email, code);
        return ResponseEntity.ok("인증 완료");
    }


}
