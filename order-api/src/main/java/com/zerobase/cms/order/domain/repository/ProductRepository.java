package com.zerobase.cms.order.domain.repository;

import com.zerobase.cms.order.domain.model.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

  //Test 시 Product 를 조회할때 ProductItems 도 함께 조회할수 있도록 (Lazy fetch 를 사용했기 때문에)
  @EntityGraph(attributePaths = {"productItems"}, type = EntityGraph.EntityGraphType.LOAD)
  Optional<Product> findBySellerIdAndId(Long sellerId, Long id);

  @EntityGraph(attributePaths = {"productItems"}, type = EntityGraph.EntityGraphType.LOAD)
  Optional<Product> findWithProductItemsById(long id);
}