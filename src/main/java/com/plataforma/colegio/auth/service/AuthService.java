package com.plataforma.colegio.auth.service;

import com.plataforma.colegio.auth.dto.LoginRequestDTO;
import com.plataforma.colegio.auth.dto.LoginResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        return new LoginResponseDTO("token-placeholder", "Usuario autenticado");
    }
}
