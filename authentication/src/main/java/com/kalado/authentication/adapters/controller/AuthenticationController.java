package com.kalado.authentication.adapters.controller;

import com.kalado.authentication.application.service.VerificationService;
import com.kalado.authentication.domain.model.AuthenticationInfo;
import com.kalado.common.dto.AuthDto;
import com.kalado.common.dto.RegistrationRequestDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.enums.Role;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.authentication.AuthenticationApi;
import com.kalado.common.response.LoginResponse;
import com.kalado.authentication.application.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

  private final AuthenticationService authService;
  private final VerificationService verificationService;

  @Override
  public LoginResponse login(String username, String password) {
    var user = authService.findByUsername(username);
    if (user != null && !verificationService.isEmailVerified(user)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED, "Email not verified");
    }
    return authService.login(username, password);
  }

  @PostMapping("/auth/verify")
  public String verifyEmail(@RequestParam String token) {
    boolean verified = verificationService.verifyEmail(token);
    if (verified) {
      return "Email verified successfully";
    }
    return "Invalid or expired token";
  }

  @PostMapping("/auth/resend-verification")
  public String resendVerificationToken(@RequestParam String username) {
    var user = authService.findByUsername(username);
    if (user != null && !verificationService.isEmailVerified(user)) {
      verificationService.resendVerificationToken(user);
      return "Verification code sent";
    }
    return "Invalid request or email already verified";
  }

  @Override
  public AuthDto validate(String token) {
    return authService.validateToken(token);
  }

  @Override
  public String getUsername(Long userId) {
    return authService.getUsername(userId);
  }

  @Override
  public void logout(String token) {
    authService.invalidateToken(token);
  }

  @Override
  @PostMapping("/auth/register")
  public void register(@RequestBody RegistrationRequestDto registrationRequest) {
    authService.register(registrationRequest);
  }
}