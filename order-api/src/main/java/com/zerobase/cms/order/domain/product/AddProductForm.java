package com.zerobase.cms.order.domain.product;

import com.zerobase.cms.order.domain.model.ProductItem;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductForm {

    private String name;
    private String description;
    private List<AddProductItemForm> items;

}
