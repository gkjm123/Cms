package com.zerobase.cms.order.controller;

import com.zerobase.cms.order.domain.product.AddProductForm;
import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.product.ProductDto;
import com.zerobase.cms.order.domain.product.ProductItemDto;
import com.zerobase.cms.order.domain.product.UpdateProductForm;
import com.zerobase.cms.order.domain.product.UpdateProductItemForm;
import com.zerobase.cms.order.service.ProductItemService;
import com.zerobase.cms.order.service.ProductService;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/product")
@RequiredArgsConstructor
public class SellerProductController {

  private final ProductService productService;
  private final JwtAuthenticationProvider provider;
  private final ProductItemService productItemService;

  //판매자가 상품 등록
  @PostMapping
  public ResponseEntity<ProductDto> addProduct(
      @RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody AddProductForm form
  ) {
    return ResponseEntity.ok(
        ProductDto.from(productService.addProduct(provider.getUserVo(token).getId(), form)));
  }

  //상품 업데이트
  @PutMapping
  public ResponseEntity<ProductDto> updateProduct(
      @RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody UpdateProductForm form
  ) {
    return ResponseEntity.ok(
        ProductDto.from(productService.updateProduct(provider.getUserVo(token).getId(), form)));
  }

  //상품 삭제
  @DeleteMapping
  public ResponseEntity<Void> deleteProduct(
      @RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestParam Long id
  ) {
    productService.deleteProduct(provider.getUserVo(token).getId(), id);
    return ResponseEntity.ok().build();
  }

  //특정 상품에 달린 옵션을 추가
  @PostMapping("/item")
  public ResponseEntity<ProductDto> addProductItem(
      @RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody AddProductItemForm form
  ) {
    return ResponseEntity.ok(ProductDto.from(
        productItemService.addProductItem(provider.getUserVo(token).getId(), form)));
  }

  //상품 옵션 변경
  @PutMapping("/item")
  public ResponseEntity<ProductItemDto> updateProductItem(
      @RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody UpdateProductItemForm form
  ) {
    return ResponseEntity.ok(ProductItemDto.from(
        productItemService.updateProductItem(provider.getUserVo(token).getId(), form)));
  }

  //상품 옵션 삭제
  @DeleteMapping("/item")
  public ResponseEntity<Void> deleteProductItem(
      @RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestParam Long id
  ) {
    productItemService.deleteProductItem(provider.getUserVo(token).getId(), id);
    return ResponseEntity.ok().build();
  }
}
