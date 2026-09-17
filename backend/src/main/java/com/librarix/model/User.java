package com.librarix.model;

import com.librarix.model.enums.Role;
import com.librarix.model.enums.UserTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String fullName;

    private Role role;

    @Builder.Default
    private UserTier userTier = UserTier.REGULAR;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
