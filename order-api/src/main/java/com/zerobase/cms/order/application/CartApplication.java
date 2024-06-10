package com.zerobase.cms.order.application;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CartApplication {
    private final CartService cartService;
    private final ProductSearchService productSearchService;

    public Cart addCart(Long customerId, AddProductCartForm form) {

        Product product = productSearchService.getByProductId(form.getId());
        if (product == null) {
            throw new CustomException(ErrorCode.CART_CHANGE_FAIL);
        }

        Cart cart = cartService.getCart(customerId);

        if (cart == null) {
            return cartService.addCart(customerId, form);
        }

        if (cart.getProducts().stream().noneMatch(p -> p.getId().equals(form.getId()))) {
            return cartService.addCart(customerId, form);
        }

        if (!addAble(cart, product, form)) {
            throw new CustomException(ErrorCode.ITEM_COUNT_NOT_ENOUGH);
        }

        return cartService.addCart(customerId, form);
    }

    public Cart updateCart(Long customerId, Cart cart) {
        cartService.putCart(customerId, cart);
        return getCart(customerId);
    }

    public Cart getCart(Long customerId) {
        Cart cart = refreshCart(cartService.getCart(customerId));
        Cart returnCart = new Cart();
        returnCart.setCustomerId(customerId);
        returnCart.setProducts(cart.getProducts());
        returnCart.setMessages(cart.getMessages());
        cart.setMessages(new ArrayList<>());
        cartService.putCart(customerId, cart);
        return returnCart;
    }

    protected Cart refreshCart(Cart cart) {

        Map<Long, Product> productMap = productSearchService.getListByProductIds(
                cart.getProducts().stream().map(Cart.Product::getId).toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for(int i = 0; i < cart.getProducts().size(); i++) {
            Cart.Product cartProduct = cart.getProducts().get(i);

            Product p = productMap.get(cartProduct.getId());
            if(p == null) {
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + " 상품이 삭제되었습니다.");
                continue;
            }

            Map<Long, ProductItem> productItemMap = p.getProductItems().stream()
                    .collect(Collectors.toMap(ProductItem::getId, item -> item));

            List<String> tmpMessage = new ArrayList<>();
            for (int j = 0; j < cartProduct.getItems().size(); j++) {
                Cart.ProductItem cartProductItem = cartProduct.getItems().get(j);
                ProductItem pi = productItemMap.get(cartProductItem.getId());
                if(pi == null) {
                    cartProduct.getItems().remove(cartProductItem);
                    j--;
                    tmpMessage.add(cartProduct.getName() + " : " +cartProductItem.getName() + " 옵션이 삭제되었습니다.");
                    continue;
                }

                boolean isPriceChanged = false, isCountNotEnough = false;

                if (!cartProductItem.getPrice().equals(pi.getPrice())) {
                    isPriceChanged = true;
                    cartProductItem.setPrice(pi.getPrice());
                }

                if (cartProductItem.getCount() > pi.getCount()) {
                    isCountNotEnough = true;
                    cartProductItem.setCount(pi.getCount());
                }

                if(isPriceChanged && isCountNotEnough) {
                    tmpMessage.add(cartProduct.getName() + " : " +cartProductItem.getName() + "\n" +
                            "가격이 변동되었습니다.\n" +
                            "수량이 부족하여 구매가능한 최대치로 변경됩니다.");
                } else if(isPriceChanged) {
                    tmpMessage.add(cartProduct.getName() + " : " +cartProductItem.getName() + "가격이 변동되었습니다.");
                } else if(isCountNotEnough) {
                    tmpMessage.add(cartProduct.getName() + " : " +cartProductItem.getName() + "수량이 부족하여 구매가능한 최대치로 변경됩니다.");
                }
            }

            if(cartProduct.getItems().isEmpty()) {
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + "상품의 옵션이 모두 없어져 구매가 불가능합니다.");
                continue;
            }

            if(!tmpMessage.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append(cartProduct.getName() + "상품의 변동사항: ");
                for (String message : tmpMessage) {
                    builder.append("\n" + message);
                }
                cart.addMessage(builder.toString());
            }
        }

        return cart;
    }

    private boolean addAble(Cart cart, Product product, AddProductCartForm form) {
        Optional<Cart.Product> optionalCartProduct = cart.getProducts().stream().filter(p -> p.getId().equals(form.getId())).findFirst();

        Cart.Product cartProduct = optionalCartProduct.get();

        System.out.println(product.getProductItems().get(1).toString());

        Map<Long, Integer> cartItemCountMap = cartProduct.getItems().stream()
                .collect(Collectors.toMap(Cart.ProductItem::getId, Cart.ProductItem::getCount));
        Map<Long, Integer> currentItemCountMap = product.getProductItems().stream()
                .collect(Collectors.toMap(ProductItem::getId, ProductItem::getCount));

        return form.getItems().stream().noneMatch(
                formItem -> {
                    Integer cartCount;
                    Integer currentCount;
                    if (cartItemCountMap.get(formItem.getId()) == null) {
                        cartCount = 0;
                    }
                    else {
                        cartCount = cartItemCountMap.get(formItem.getId());
                    }

                    if (currentItemCountMap.get(formItem.getId()) == null) {
                        throw new CustomException(ErrorCode.PRODUCT_ITEM_NOT_FOUND);
                    }
                    else {
                        currentCount = currentItemCountMap.get(formItem.getId());
                    }

                    return formItem.getCount() + cartCount > currentCount;
                });
    }
}
