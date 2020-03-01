package com.github.tangyi.auth.security;

import com.github.tangyi.common.core.exceptions.TenantNotFoundException;
import com.github.tangyi.common.security.core.CustomUserDetailsService;
import com.github.tangyi.common.security.event.CustomAuthenticationFailureEvent;
import com.github.tangyi.common.security.event.CustomAuthenticationSuccessEvent;
import com.github.tangyi.common.security.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 认证Provider，提供获取用户信息、认证、授权等功能
 *
 * @author tangyi
 * @date 2019/5/28 21:26
 */
@Slf4j
public class CustomUserDetailsAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private PasswordEncoder passwordEncoder;

    private CustomUserDetailsService userDetailsService;

    private String userNotFoundEncodedPassword;

	private ApplicationEventPublisher publisher;

    public CustomUserDetailsAuthenticationProvider(PasswordEncoder passwordEncoder, CustomUserDetailsService userDetailsService, ApplicationEventPublisher publisher) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.publisher = publisher;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            log.debug("Authentication failed: password is blank");
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "密码为空"));
        }
        // 获取密码
        String presentedPassword = authentication.getCredentials().toString();
        // 匹配密码
        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            log.debug("Authentication failed: invalid password");
			publisher.publishEvent(new CustomAuthenticationFailureEvent(authentication, userDetails));
			throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "用户名或密码错误"));
        }
		publisher.publishEvent(new CustomAuthenticationSuccessEvent(authentication, userDetails));
	}

    @Override
    protected void doAfterPropertiesSet() throws Exception {
        if (this.userDetailsService == null)
            throw new IllegalArgumentException("UserDetailsService can not be null");
        this.userNotFoundEncodedPassword = this.passwordEncoder.encode("userNotFoundPassword");
    }

    /**
     * 加载用户信息
     *
     * @param username       username
     * @param authentication authentication
     * @return UserDetails
     * @throws AuthenticationException
     */
    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException, TenantNotFoundException{
        UserDetails loadedUser;
        try {
            // 加载用户信息
            loadedUser = this.userDetailsService.loadUserByIdentifierAndTenantCode(TenantContextHolder.getTenantCode(), authentication.getPrincipal().toString());
        } catch (UsernameNotFoundException notFound) {
            if (authentication.getCredentials() != null) {
                String presentedPassword = authentication.getCredentials().toString();
                passwordEncoder.matches(presentedPassword, userNotFoundEncodedPassword);
            }
            throw notFound;
        } catch (Exception tenantNotFound) {
			throw new InternalAuthenticationServiceException(tenantNotFound.getMessage(), tenantNotFound);
        }
        if (loadedUser == null) {
            throw new InternalAuthenticationServiceException("get user information failed");
        }
        return loadedUser;
    }
}
