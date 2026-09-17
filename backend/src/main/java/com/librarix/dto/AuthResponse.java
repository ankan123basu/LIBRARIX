package com.librarix.dto;

import com.librarix.model.enums.Role;
import com.librarix.model.enums.UserTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private String id;
    private String email;
    private String fullName;
    private Role role;
    private UserTier userTier;
}
