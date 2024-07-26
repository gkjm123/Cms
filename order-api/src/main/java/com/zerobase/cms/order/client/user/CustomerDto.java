package com.zerobase.cms.order.client.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerDto {

  private Long id;
  private String email;
  private Integer balance;
}
