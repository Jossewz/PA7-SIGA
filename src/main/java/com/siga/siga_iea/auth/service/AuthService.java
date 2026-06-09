package com.siga.siga_iea.auth.service;

import com.siga.siga_iea.auth.dto.LoginRequestDTO;
import com.siga.siga_iea.auth.dto.LoginResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        return new LoginResponseDTO("token-placeholder", "Usuario autenticado");
    }
}

