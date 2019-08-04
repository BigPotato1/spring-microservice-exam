package com.github.tangyi.msc.controller;

import com.github.tangyi.common.core.model.ResponseBean;
import com.github.tangyi.common.core.web.BaseController;
import com.github.tangyi.msc.api.dto.SmsDto;
import com.github.tangyi.msc.api.model.SmsResponse;
import com.github.tangyi.msc.service.SmsService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送短信接口
 *
 * @author tangyi
 * @date 2019/6/22 12:59
 */
@Slf4j
@AllArgsConstructor
@Api("发送短信")
@RestController
@RequestMapping(value = "/v1/sms")
public class SmsController extends BaseController {

    private final SmsService smsService;

    /**
     * 发送短信
     *
     * @param smsDto smsDto
     * @return ResponseBean
     * @author tangyi
     * @date 2019/06/22 13:12
     */
    @PostMapping("sendSms")
    public ResponseBean<SmsResponse> sendSms(@RequestBody SmsDto smsDto) {
        log.info("发送短信给{}，发送内容：{}", smsDto.getReceiver(), smsDto.getContent());
        SmsResponse smsResponse = smsService.sendSms(smsDto);
        log.info("发送短信成功，返回内容：{}", smsResponse);
        return new ResponseBean<>(smsResponse);
    }
}
