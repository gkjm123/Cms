package com.zerobase.cms.order.domain.product;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductItemForm {
    private Long id;
    private Long productId;
    private String name;
    private Integer price;
    private Integer count;
}
