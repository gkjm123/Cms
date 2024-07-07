package com.zerobase.cms.user.config.filter;

import com.zerobase.cms.user.service.seller.SellerService;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import com.zerobase.domain.domain.common.UserVo;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@WebFilter(urlPatterns = "/seller/*")
@RequiredArgsConstructor
public class SellerFilter implements Filter {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final SellerService sellerService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("X-AUTH-TOKEN");

        //토큰 파싱해보고 유효한지 판단
        if (!jwtAuthenticationProvider.validateToken(token)) {
            throw new ServletException("토큰이 유효하지 않습니다.");
        }

        //사용자의 Id, 이메일 정보
        UserVo vo = jwtAuthenticationProvider.getUserVo(token);

        //토큰 파싱해서 얻은 사용자 정보가 DB 에 있는지 확인
        sellerService.findByIdAndEmail(vo.getId(), vo.getEmail())
                .orElseThrow(() -> new ServletException("토큰이 유효하지 않니다."));

        chain.doFilter(request, response);
    }
}
