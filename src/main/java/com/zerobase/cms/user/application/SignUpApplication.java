package com.zerobase.cms.user.application;

import com.zerobase.cms.user.client.MailgunClient;
import com.zerobase.cms.user.client.mailgun.SendMailForm;
import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import com.zerobase.cms.user.service.customer.SignUpCustomerService;
import com.zerobase.cms.user.service.seller.SignUpSellerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpApplication {
    private final MailgunClient mailgunClient;
    private final SignUpCustomerService signUpCustomerService;
    private final SignUpSellerService signUpSellerService;

    public void customerVerify(String email, String code) {
        signUpCustomerService.verifyEmail(email, code);
    }

    public void sellerVerify(String email, String code) {
        signUpSellerService.verifyEmail(email, code);
    }

    public String customerSignUp(SignUpForm form) {
        if(signUpCustomerService.isEmailExist(form.getEmail())) {
            throw new CustomException(ErrorCode.ALREADY_REGISTER_USER);
        }

        Customer c = signUpCustomerService.signUp(form);
        String code = getRandomCode();

        SendMailForm sendMailForm = SendMailForm.builder()
                .from("gkje123@naver.com")
                .to(c.getEmail())
                .subject("Verificationi Email")
                .text(getVerificationEmailBody(c.getEmail(), c.getName(), "customer", code))
                .build();

        mailgunClient.sendEmail(sendMailForm);
        signUpCustomerService.changeCustomerValidateEmail(c.getId(), code);
        return "회원가입 성공";
    }

    public String sellerSignUp(SignUpForm form) {
        if(signUpSellerService.isEmailExist(form.getEmail())) {
            throw new CustomException(ErrorCode.ALREADY_REGISTER_USER);
        }
        else {
            Seller c = signUpSellerService.signUp(form);

            String code = getRandomCode();

            SendMailForm sendMailForm = SendMailForm.builder()
                    .from("gkje123@naver.com")
                    .to(c.getEmail())
                    .subject("Verificationi Email")
                    .text(getVerificationEmailBody(c.getEmail(), c.getName(), "seller", code))
                    .build();

            mailgunClient.sendEmail(sendMailForm);

            signUpSellerService.changeSellerValidateEmail(c.getId(), code);
            return "회원가입 성공";
        }
    }

    private String getRandomCode() {
        return RandomStringUtils.random(10, true, true);
    }

    private String getVerificationEmailBody(String email, String name, String type, String code) {
        return "Hello " + name + "! 메일 인증을 위해 링크를 클릭해주세요.\n\n" +
                "http://localhost:8081/signup/" + type + "/verify?email=" + email + "&code=" + code;
    }
}
