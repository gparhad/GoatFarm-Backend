package com.goatfarm.service;

import com.goatfarm.entity.User;
import com.goatfarm.mapper.UserMapper;
import com.goatfarm.model.UserData;
import com.goatfarm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserData createUser(UserData user) {
        // Sanitize: Treat empty string as null so it doesn't trigger unique constraint collisions
        String email = cleanEmail(user.getEmail());
        user.setEmail(email);

        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already in use.");
        }

        User userEntity = userRepository.save(UserMapper.toUser(user, true));
        return UserMapper.toUserDTO(userEntity, false);
    }

    public UserData updateUser(UserData user, Long userId) {
        String email = cleanEmail(user.getEmail());
        user.setEmail(email);

        if (email != null && userRepository.existsByEmailAndUserIdNot(email, userId)) {
            throw new IllegalArgumentException("Email '" + email + "' is already in use by another user.");
        }

        User userEntity = userRepository.save(UserMapper.toUserForUpdate(user, userId));
        return UserMapper.toUserDTO(userEntity, false);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public UserData getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> UserMapper.toUserDTO(user, false))
                .orElseGet(UserData::new);
    }

    public UserData findByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(user -> UserMapper.toUserDTO(user, true))
                .orElseGet(UserData::new);
    }

    private String cleanEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}