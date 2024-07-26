package com.zerobase.cms.user.client;

import com.zerobase.cms.user.client.mailgun.SendMailForm;
import com.zerobase.cms.user.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "mailgun", url = "https://api.mailgun.net/v3/", configuration = FeignConfig.class)
public interface MailgunClient {

  @PostMapping("sandbox938fb9033ee4442ebb21ffc7d7feeac0.mailgun.org/messages")
  ResponseEntity<String> sendEmail(@SpringQueryMap SendMailForm form);
}