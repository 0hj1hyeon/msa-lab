package com.distributed.userservice.dto;

import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String name;
    private String password;
    private String username;

    private String userId;
    private String encryptedPwd;
}