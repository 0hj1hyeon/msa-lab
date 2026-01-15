package com.distributed.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class ServerInfoController {

    @GetMapping("/info")
    public String getServerInfo() {
        try {
            // 현재 실행 중인 서버(컨테이너)의 호스트네임을 반환합니다.
            // Docker 환경에서는 컨테이너 ID가 호스트네임이 됩니다.
            return "Response from Server: " + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }
}
