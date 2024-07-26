package com.zerobase.cms.order.domain.model;

import com.zerobase.cms.order.domain.product.AddProductItemForm;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@AuditOverride(forClass = BaseEntity.class)
public class ProductItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sellerId;

  @Audited
  private String name;

  @Audited
  private Integer price;

  private Integer count;

  @ManyToOne
  private Product product;

  public static ProductItem of(Long sellerId, AddProductItemForm form) {
    return ProductItem.builder()
        .sellerId(sellerId)
        .name(form.getName())
        .price(form.getPrice())
        .count(form.getCount())
        .build();
  }
}
