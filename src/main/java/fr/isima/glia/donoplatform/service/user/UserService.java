package fr.isima.glia.donoplatform.service.user;

import fr.isima.glia.donoplatform.controller.api.AnnouncementApiController;
import fr.isima.glia.donoplatform.controller.api.BatchApiController;
import fr.isima.glia.donoplatform.controller.api.FavoriteApiController;
import fr.isima.glia.donoplatform.controller.api.UserApiController;
import fr.isima.glia.donoplatform.dto.UserDto;
import fr.isima.glia.donoplatform.exceptions.AlreadyExistsException;
import fr.isima.glia.donoplatform.exceptions.UserNotFoundException;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.repository.UserRepository;
import fr.isima.glia.donoplatform.request.CreateUserRequest;
import fr.isima.glia.donoplatform.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    public UserDto convertToDto(User user) {
        UserDto dto = modelMapper.map(user, UserDto.class);

        // ============================================================
        // HATEOAS - Ajout des liens hypermedia
        // ============================================================
        try {
            // Self link
            dto.add(linkTo(methodOn(UserApiController.class)
                    .getUserById(user.getId(), null))
                    .withSelfRel());

            // Announcements link
            dto.add(linkTo(methodOn(AnnouncementApiController.class)
                    .getUserAnnouncements(user.getId()))
                    .withRel("announcements"));

            // Favorites link
            dto.add(linkTo(methodOn(FavoriteApiController.class)
                    .getUserFavorites(user.getId(), null))
                    .withRel("favorites"));

            // Batches link
            dto.add(linkTo(methodOn(BatchApiController.class)
                    .getBatchesByUser(user.getId(), null))
                    .withRel("batches"));

            // Update link
            dto.add(linkTo(methodOn(UserApiController.class)
                    .updateUser(user.getId(), null, null))
                    .withRel("update"));

            // Delete link
            dto.add(linkTo(methodOn(UserApiController.class)
                    .deleteUser(user.getId(), null))
                    .withRel("delete"));

        } catch (Exception e) {
            // Si erreur de génération de lien, on continue sans bloquer
        }

        return dto;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = getUserById(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AlreadyExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
