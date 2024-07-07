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
        //카트의 상품들과 매칭되는 실제 상품들을 가져오고 <상품 ID, 상품> 의 Map 으로 변환
        Map<Long, Product> productMap = productSearchService.getListByProductIds(
                cart.getProducts().stream().map(Cart.Product::getId).toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        //카트의 상품들을 하나씩 돌면서 실제 상품과 비교해보며 변경사항 체크
        for(int i = 0; i < cart.getProducts().size(); i++) {
            Cart.Product cartProduct = cart.getProducts().get(i);
            Product p = productMap.get(cartProduct.getId());

            //매칭된 실제 상품이 없을 경우
            if(p == null) {
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + " 상품이 삭제되었습니다.");
                continue;
            }

            //상품의 ProductItem(옵션들) 을 <옵션 ID, 옵션> 의 Map 으로 변환
            Map<Long, ProductItem> productItemMap = p.getProductItems().stream()
                    .collect(Collectors.toMap(ProductItem::getId, item -> item));

            //변경사항을 저장할 message List
            List<String> tmpMessage = new ArrayList<>();

            //상품의 옵션들을 돌면서 실제 상품의 옵션과 비교, 변경사항 체크
            for (int j = 0; j < cartProduct.getItems().size(); j++) {
                Cart.ProductItem cartProductItem = cartProduct.getItems().get(j);
                ProductItem pi = productItemMap.get(cartProductItem.getId());

                //실제 상품의 옵션이 없어졌다.
                if(pi == null) {
                    cartProduct.getItems().remove(cartProductItem);
                    j--;
                    tmpMessage.add(cartProduct.getName() + " : " +cartProductItem.getName() + " 옵션이 삭제되었습니다.");
                    continue;
                }

                //실제 상품의 옵션과 비교했을대 가격이 변경되었는지, 구매하려는 수량(기존 카트 수량+새로 담은 수량) 이 실제 수량보다 큰지 체크
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

            //실제 상품 옵션이 삭제되지는 않았지만 수량이 0 인 상태이다.
            if(cartProduct.getItems().isEmpty()) {
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + " 상품의 옵션이 모두 없어져 구매가 불가능합니다.");
                continue;
            }

            //담긴 메시지가 하나라도 있다면 카트의 메시지 부분에 다 넣어준다.
            if(!tmpMessage.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append(cartProduct.getName() + " 상품의 변동사항: ");
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
