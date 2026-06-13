package com.goatfarm.service;

import com.goatfarm.entity.User;
import com.goatfarm.mapper.EntityMapper;
import com.goatfarm.model.FarmData;
import com.goatfarm.model.UserData;
import com.goatfarm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserData createUser(UserData user) {
        User userEntity = userRepository.save(EntityMapper.toUser(user));
        return EntityMapper.toUserDTO(userEntity);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserData getUserById(Long id) {
        User userEntity =  userRepository.findById(id).orElse(new User());
        return EntityMapper.toUserDTO(userEntity);
    }

    public UserData findByUsername(String username) {
        Optional<User> userEntity = userRepository.findByUsername(username);
        UserData userData =   userEntity.isPresent()? EntityMapper.toUserDTO(userEntity.get()):new UserData();
        return userData;
    }

}
