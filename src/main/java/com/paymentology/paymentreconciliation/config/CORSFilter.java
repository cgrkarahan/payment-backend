package com.paymentology.paymentreconciliation.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component
@Slf4j
public class CORSFilter implements Filter {


    public CORSFilter() {
        log.info("SimpleCORSFilter init");
    }

    /**

     This method is an implementation of the doFilter method defined in the Filter interface. It sets the necessary HTTP headers to enable cross-origin resource sharing (CORS) in the response object for incoming requests.
     @param req A ServletRequest object that represents the request the client has made of the servlet.
     @param res A ServletResponse object that represents the response the servlet sends to the client.
     @param chain A FilterChain object that is used to invoke the next filter in the chain, or if the calling filter is the last filter in the chain, to invoke the resource at the end of the chain.
     @throws IOException if an I/O related error occurs during the processing of the request or response.
     @throws ServletException if a servlet related error occurs during the processing of the request or response.
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");

        chain.doFilter(req, res);
    }


}