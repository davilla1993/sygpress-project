package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.response.UserResponse;
import com.follysitou.sygpress.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setPublicId(user.getPublicId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());

        // Construire fullName pour le frontend
        String fullName = "";
        if (user.getFirstName() != null) {
            fullName = user.getFirstName();
        }
        if (user.getLastName() != null) {
            fullName = fullName.isEmpty() ? user.getLastName() : fullName + " " + user.getLastName();
        }
        response.setFullName(fullName.isEmpty() ? user.getUsername() : fullName);

        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setEnabled(user.getEnabled());
        response.setActive(user.getEnabled()); // Alias pour le frontend
        response.setMustChangePassword(user.getMustChangePassword());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
