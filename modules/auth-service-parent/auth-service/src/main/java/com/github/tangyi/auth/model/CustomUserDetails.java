package com.github.tangyi.auth.model;

import com.github.tangyi.common.core.enums.LoginType;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 用户信息
 *
 * @author tangyi
 * @date 2019/5/28 21:13
 */
@Data
public class CustomUserDetails extends User {

    private static final long serialVersionUID = 1L;

    /**
     * 租户标识
     */
    private String tenantCode;

    /**
     * 开始授权时间
     */
    private long start;

    /**
     * 登录类型
     */
    private LoginType loginType;

    /**
     * 构造方法
     *
     * @param username    username
     * @param password    password
     * @param enabled     enabled
     * @param authorities authorities
     * @param tenantCode  tenantCode
     * @param start       start
     * @param loginType   loginType
     */
    public CustomUserDetails(String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities, String tenantCode, long start, LoginType loginType) {
        super(username, password, enabled, true, true, true, authorities);
        this.tenantCode = tenantCode;
        this.start = start;
        this.loginType = loginType;
    }
}
