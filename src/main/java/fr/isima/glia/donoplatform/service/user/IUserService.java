package fr.isima.glia.donoplatform.service.user;

import fr.isima.glia.donoplatform.dto.UserDto;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.request.CreateUserRequest;
import fr.isima.glia.donoplatform.request.UserUpdateRequest;

import java.util.List;

public interface IUserService {
    User createUser(CreateUserRequest request);
    User getUserById(Long id);
    User getUserByUsername(String username);
    UserDto convertToDto(User user);
    User updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    List<User> getAllUsers();
}
