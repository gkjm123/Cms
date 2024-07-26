package com.zerobase.cms.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  DATE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 유효기간이 지났습니다."),
  NOT_ENOUGH_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
  NOT_FOUND_USER(HttpStatus.BAD_REQUEST, "유저 정보를 찾을수 없습니다."),
  SIGN_IN_ERROR(HttpStatus.BAD_REQUEST, "로그인 정보가 잘못되었습니다."),
  CODE_NOT_MATCH(HttpStatus.BAD_REQUEST, "잘못된 인증 시도입니다."),
  ALREADY_VERIFY(HttpStatus.BAD_REQUEST, "이미 인증되었습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원정보를 찾을수 없습니다."),
  ALREADY_REGISTER_USER(HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다.");

  private final HttpStatus status;
  private final String detail;
}
