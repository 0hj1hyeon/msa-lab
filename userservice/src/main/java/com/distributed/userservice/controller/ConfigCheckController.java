package com.distributed.userservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope // 나중에 2번(실시간 갱신)을 위해 미리 붙여둡니다.
public class ConfigCheckController {

    @Value("${test.value:연결실패}") // 깃허브 userservice.properties에 적어둔 키값
    private String configValue;

    @GetMapping("/config/check")
    public String check() {
        return "Config Server에서 가져온 값: " + configValue;
    }
}