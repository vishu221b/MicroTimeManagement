package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.converter.UserDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementUserException;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.UserRepository;
import com.microtimemanagement.apiservice.service.RoleService;
import com.microtimemanagement.apiservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final RoleService roleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDTOConverter userConverter;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findActiveUserByEmailOrUsername(username);;
        user.setRoles(roleService.getRoleNamesForIds(user.getRoles()));
        return user;
    }

    private void validateIfUserAlreadyExistsByUsernameOrEmail(String username, String email, Boolean isUpdate){
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
    }

    @Override
    public GenericMessageResponseDTO<?> createNewUser(NewUserRequestDTO requestDTO) {
        User newUser = User.builder()
                .email(requestDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(requestDTO.getPassword()))
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .username(requestDTO.getUsername())
                .dateOfBirth(requestDTO.getDateOfBirth())
                .roles(roleService.getDefaultUserRoleIds())
                .build();
        validateIfUserAlreadyExistsByUsernameOrEmail(newUser.getUsername(), newUser.getEmail(), Boolean.FALSE);
        userRepository.save(newUser);
        return GenericMessageResponseDTO.builder().payload(NewUserResponseDTO.builder()
                .emailAddress(newUser.getEmail())
                .username(newUser.getUsername())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .createdAt(newUser.getCreatedAt())
                .dateOfBirth(newUser.getDateOfBirth())
                .build()).message(ResponseMessages.USER_REGISTRATION_SUCCESS).build();
    }

    private User findById(String id){
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
        }
        return user.get();
    }

    private User findActiveById(String id){
        return userRepository.findByIdAndIsActiveTrue(id);
    }
    private User findByUsername(String username){
        Optional<User> user = userRepository.findByUsernameAndIsActiveTrue(username);
        if(user.isEmpty()){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
        }
        return user.get();
    }
    private User findByEmail(String email){
        Optional<User> user = userRepository.findByEmailAndIsActiveTrue(email);
        if(user.isEmpty()){
            throw new MicroTimeManagementUserException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
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
                // Used for saving new roles from Admin Controller
                return userConverter.toDTO(userRepository.save(saveUser));
            // Only change the allowed fields in update request
            saveUser = findActiveById(userDTO.getId());
            if(null == saveUser)
                throw new MicroTimeManagementNotFoundException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
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

    private User saveUser(User user){
        log.info("Saving user: {}", user);
        return userRepository.save(user);
    }

    @Override
    public GenericMessageResponseDTO<?> updateUserDetails(UserDetailsUpdateRequestDTO userDetailsUpdateRequestDTO) {
        User currentUser = userRepository.findByUidAndIsActiveTrue(userDetailsUpdateRequestDTO.getUid());

        if(null == currentUser){
            throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_USER_IDENTIFIER_VALUE);
        }

        // UIDs are used to uniquely identify the users
        if(!currentUser.getUid().equals(userDetailsUpdateRequestDTO.getUid())){
            throw new MicroTimeManagementUserException(ErrorConstants.CANNOT_UPDATE_UID_OF_EXISTING_USER);
        }
        // Email, username, First Name, Last Name, DOB changes are allowed
        log.info("Update user request: {}", userDetailsUpdateRequestDTO);
        if(!userDetailsUpdateRequestDTO.getFirstName().equals(currentUser.getFirstName())){
            currentUser.setFirstName(userDetailsUpdateRequestDTO.getFirstName());
        }

        if(!userDetailsUpdateRequestDTO.getLastName().equals(currentUser.getLastName())){
            currentUser.setLastName(userDetailsUpdateRequestDTO.getLastName());
        }

        if(!userDetailsUpdateRequestDTO.getDateOfBirth().equals(currentUser.getDateOfBirth())){
            currentUser.setDateOfBirth(userDetailsUpdateRequestDTO.getDateOfBirth());
        }

        if(!userDetailsUpdateRequestDTO.getUsername().equals(currentUser.getUsername())){
            currentUser.setUsername(userDetailsUpdateRequestDTO.getUsername());
        }

        if(!userDetailsUpdateRequestDTO.getEmail().equals(currentUser.getEmail())){
            currentUser.setEmail(userDetailsUpdateRequestDTO.getEmail());
        }

        log.info("Updated user details: {}", currentUser);

        return  GenericMessageResponseDTO.builder()
                .payload(userConverter.toDTO(saveUser(currentUser)))
                .message(ResponseMessages.USER_DETAILS_UPDATED)
                .build();
    }

    @Override
    public GenericMessageResponseDTO<?> deleteUserByUsername(String username) {
        User user = findByUsername(username);
        user.setIsActive(Boolean.FALSE);
        userRepository.save(user);
        return GenericMessageResponseDTO.builder()
                .message(ResponseMessages.ACCOUNT_DELETED_SUCCESSFULLY)
                .build();
    }

    private User findActiveUserByEmailOrUsername(String username){
        Optional<User> user = userRepository.findByEmailOrUsernameAndIsActiveTrue(username, username);
        if(user.isEmpty())
            throw new MicroTimeManagementNotFoundException(
                    String.format(ErrorConstants.ACCOUNT_NOT_FOUND_FOR_USERNAME, username));
        return user.get();
    }
    @Override
    public GenericMessageResponseDTO<?> changeUserPassword(PasswordChangeRequestDTO passwordChangeRequestDTO) {
        User user = findActiveUserByEmailOrUsername(passwordChangeRequestDTO.getUsername());
        String message = ErrorConstants.SOMETHING_WENT_WRONG;
        if(bCryptPasswordEncoder.matches(passwordChangeRequestDTO.getOldPassword(), user.getPassword())){
            user.setPassword(bCryptPasswordEncoder.encode(passwordChangeRequestDTO.getNewPassword()));
            message = ResponseMessages.PASSWORD_CHANGED_SUCCESSFULLY;
        }
        return GenericMessageResponseDTO.builder()
                .message(message)
                .build();
    }

    @Override
    public UserDTO getUserDTOByUid(String id) {
        return userConverter.toDTO(getUserByUid(id));
    }

    @Override
    public User getUserByUid(String id) {
        User user = userRepository.findByUidAndIsActiveTrue(id);
        if(null == user)
            throw new MicroTimeManagementNotFoundException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
        return user;
    }


    @Override
    public UserDTO getUserProfile(User user) {
        UserDTO userDTO = userConverter.toDTO(user);
        log.info("Retrieving user profile: {}", userDTO);
        return userDTO;
    }

    @Override
    public UserDTO loadUserDTOByUsername(String username) {
        return userConverter.toDTO(findActiveUserByEmailOrUsername(username));
    }

}
