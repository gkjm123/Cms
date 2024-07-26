package com.zerobase.cms.order.domain.product;

import com.zerobase.cms.order.domain.model.ProductItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductItemDto {

  private Long id;
  private String name;
  private Integer price;
  private Integer count;

  public static ProductItemDto from(ProductItem item) {
    return new ProductItemDto(item.getId(), item.getName(), item.getPrice(), item.getCount());
  }
}