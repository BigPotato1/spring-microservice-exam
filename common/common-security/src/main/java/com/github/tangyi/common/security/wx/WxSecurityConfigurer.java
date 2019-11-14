package com.github.tangyi.common.security.wx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tangyi.common.security.core.CustomUserDetailsService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 微信登录配置
 *
 * @author tangyi
 * @date 2019/07/05 19:29
 */
@Data
public class WxSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationEventPublisher defaultAuthenticationEventPublisher;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

    private AuthenticationSuccessHandler wxLoginSuccessHandler;

    private CustomUserDetailsService userDetailsService;

    @Override
    public void configure(HttpSecurity http) {
        // 微信登录过滤器
        WxAuthenticationFilter wxAuthenticationFilter = new WxAuthenticationFilter();
        wxAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        wxAuthenticationFilter.setAuthenticationSuccessHandler(wxLoginSuccessHandler);
        wxAuthenticationFilter.setEventPublisher(defaultAuthenticationEventPublisher);
        WxAuthenticationProvider wxAuthenticationProvider = new WxAuthenticationProvider();
        wxAuthenticationProvider.setCustomUserDetailsService(userDetailsService);
		wxAuthenticationProvider.setPublisher(applicationEventPublisher);
        // 增加微信登录的过滤器
        http.authenticationProvider(wxAuthenticationProvider).addFilterAfter(wxAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
