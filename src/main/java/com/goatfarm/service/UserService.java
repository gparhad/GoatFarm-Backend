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
        // No functional change: still saves via mapper
        User userEntity = userRepository.save(UserMapper.toUser(user, true));
        return UserMapper.toUserDTO(userEntity, false);
    }

    public UserData updateUser(UserData user, Long userId) {
        // No functional change: still saves via mapper
        User userEntity = userRepository.save(UserMapper.toUserForUpdate(user, userId));
        return UserMapper.toUserDTO(userEntity, false);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public UserData getUserById(Long id) {
        // No functional change
        // previously: orElse(new User()) and then mapped
        return userRepository.findById(id)
                .map(user -> UserMapper.toUserDTO(user, false))
                .orElseGet(UserData::new);
    }

    /**
     * No functional change:
     * - If user exists -> return mapped UserData with password info (true)
     * - If not exists -> return empty UserData (new UserData())
     *
     * (AuthController treats empty user as invalid credentials -> same end result)
     */
    public UserData findByUsername(String username) {
        return userRepository.findByUserName(username)
                .map(user -> UserMapper.toUserDTO(user, true))
                .orElseGet(UserData::new);
    }
}