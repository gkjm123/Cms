package com.zerobase.cms.order.domain.product;

import com.zerobase.cms.order.domain.model.Product;
import java.util.List;
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
public class ProductDto {

  private Long id;
  private String name;
  private String description;
  private List<ProductItemDto> items;

  public static ProductDto from(Product product) {
    return new ProductDto(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getProductItems().stream().map(ProductItemDto::from).toList());
  }

  public static ProductDto withoutItemsFrom(Product product) {
    return ProductDto.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .build();
  }
}
