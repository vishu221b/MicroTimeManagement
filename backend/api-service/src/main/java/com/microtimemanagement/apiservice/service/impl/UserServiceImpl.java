package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.converter.UserDTOConverter;
import com.microtimemanagement.apiservice.dto.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementUserException;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.repository.UserRepository;
import com.microtimemanagement.apiservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDTOConverter userConverter;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> opUser = userRepository.findByEmailOrUsernameAndIsActiveTrue(username, username);
        User user;
        if(opUser.isEmpty()){
            log.error("Account not found for username '{}'. Could not authenticate user.", username);
            throw new MicroTimeManagementUserException(
                    String.format(ErrorConstants.ACCOUNT_NOT_FOUND_FOR_USERNAME, username)
            );
        }else{
            user = opUser.get();
        }
        user.setRoles(user.getRoles().stream()
                .map(r -> roleRepository.findByIdAndIsActiveTrue(r).getName()).collect(Collectors.toSet()));
        return user;
    }

    private void validateIfUserAlreadyExistsByUsernameOrEmail(UserDTO userDTO, Boolean isUpdate){
        String username = userDTO.getUsername();
        String email = userDTO.getEmail();
        Optional<User> existingUser = findOptionalByUsername(username);
        if(existingUser.isPresent() && !isUpdate){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_ALREADY_EXISTS_FOR_USERNAME);
        }
        existingUser = findOptionalByEmail(email);
        if(existingUser.isPresent() && !isUpdate){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_ALREADY_EXISTS_FOR_EMAIL);
        }
        if(isUpdate && existingUser.isEmpty()){
            throw new MicroTimeManagementNotFoundException(ErrorConstants.NO_USER_FOUND_FOR_UPDATE);
        }
        if(existingUser.isPresent() && !existingUser.get().getId().equals(userDTO.getId())){
            throw new MicroTimeManagementUserException(ErrorConstants.CANNOT_UPDATE_ID_OF_EXISTING_USER);
        }
        if(existingUser.isPresent() && !existingUser.get().getUid().equals(userDTO.getUid())){
            throw new MicroTimeManagementUserException(ErrorConstants.CANNOT_UPDATE_UID_OF_EXISTING_USER);
        }
    }

    @Override
    public NewUserResponseDTO createNewUser(NewUserRequestDTO requestDTO) {
        User newUser = User.builder()
                .email(requestDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(requestDTO.getPassword()))
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .username(requestDTO.getUsername())
                .roles(
                        Set.of(roleRepository.findByNameAndIsActiveTrue("ROLE_MTM_USER").getId()))
                .build();
        validateIfUserAlreadyExistsByUsernameOrEmail(userConverter.toDTO(newUser), Boolean.FALSE);
        userRepository.save(newUser);
        return NewUserResponseDTO.builder()
                .emailAddress(newUser.getEmail())
                .username(newUser.getUsername())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .createdAt(newUser.getCreatedAt())
                .build();
    }

    private User findById(String id){
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_NOT_FOUND);
        }
        return user.get();
    }
    private User findByUsername(String username){
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_NOT_FOUND);
        }
        return user.get();
    }
    private User findByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_NOT_FOUND);
        }
        return user.get();}

    private Optional<User> findOptionalByUsername(String username){
         return userRepository.findByUsername(username);
    }

    private Optional<User> findOptionalByEmail(String email){
        return userRepository.findByEmail(email);
    }
    @Override
    public UserDTO findDTOById(String userId) {
        return userConverter.toDTO(findById(userId));
    }

    @Override
    public UserDTO findDTOByUsername(String username) {
        return userConverter.toDTO(findByUsername(username));
    }

    @Override
    public UserDTO findDTOByEmail(String userEmail) {
        return userConverter.toDTO(findByEmail(userEmail));
    }

    @Override
    public UserDTO saveUserFromDTO(UserDTO userDTO, Boolean isUpdateRequest) {
        try{
            log.info("Saving from user DTO with isUpdateRequest {}: {}", isUpdateRequest, userDTO);
            User saveUser = userConverter.fromDTO(userDTO);
            if(!isUpdateRequest)
                return userConverter.toDTO(userRepository.save(saveUser));
            saveUser.setFirstName(userDTO.getFirstName());
            saveUser.setLastName(userDTO.getLastName());
            saveUser.setEmail(userDTO.getEmail());
            saveUser.setUsername(userDTO.getUsername());
            User savedUser = userRepository.save(saveUser);
            return userConverter.toDTO(savedUser);
        }catch (Exception e){
            log.error("Error while saving user: {}", e.getMessage());
            throw new MicroTimeManagementException(ErrorConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public UserDTO updateUserDetails(UserDTO userDTO) {
        validateIfUserAlreadyExistsByUsernameOrEmail(userDTO, Boolean.TRUE);
        log.info("Updating user with id {} to : {}", userDTO.getId(), userDTO);
        return saveUserFromDTO(userDTO, Boolean.TRUE);
    }

}
