package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.converter.UserDTOConverter;
import com.microtimemanagement.apiservice.dto.entity.RoleDTO;
import com.microtimemanagement.apiservice.dto.entity.UserDTO;
import com.microtimemanagement.apiservice.dto.request.NewUserRequestDTO;
import com.microtimemanagement.apiservice.dto.request.PasswordChangeRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UserDetailsUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.request.UsersRolesUpdateRequestDTO;
import com.microtimemanagement.apiservice.dto.response.NewUserResponseDTO;
import com.microtimemanagement.apiservice.dto.response.PaginationResultResponseDTO;
import com.microtimemanagement.apiservice.enums.UserRoleUpdateAction;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementUserException;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.UserRepository;
import com.microtimemanagement.apiservice.service.RoleService;
import com.microtimemanagement.apiservice.service.UserService;
import com.microtimemanagement.apiservice.utils.ApiUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final RoleService roleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDTOConverter userConverter;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findActiveUserByEmailOrUsername(username);;
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
    public NewUserResponseDTO createNewUser(NewUserRequestDTO requestDTO) {

        validateIfUserAlreadyExistsByUsernameOrEmail(requestDTO.getUsername(), requestDTO.getEmail(), Boolean.FALSE);

        User newUser = userRepository.save(User.builder()
                .email(requestDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(requestDTO.getPassword()))
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .username(requestDTO.getUsername())
                .dateOfBirth(requestDTO.getDateOfBirth())
                .roles(roleService.getDefaultUserRoleIds())
                .build());
        return NewUserResponseDTO.builder()
                .emailAddress(newUser.getEmail())
                .username(newUser.getUsername())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .createdAt(newUser.getCreatedAt())
                .dateOfBirth(newUser.getDateOfBirth())
                .build();
    }

    private User findById(String id){
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            throw new MicroTimeManagementNotFoundException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
        }
        return user.get();
    }

    private User findActiveById(String id){
        return userRepository.findByIdAndIsActiveTrue(id);
    }
    private User findByUsername(String username){
        Optional<User> user = userRepository.findByUsernameAndIsActiveTrue(username);
        if(user.isEmpty()){
            throw new MicroTimeManagementNotFoundException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
        }
        return user.get();
    }
    private User findByEmail(String email){
        Optional<User> user = userRepository.findByEmailAndIsActiveTrue(email);
        if(user.isEmpty()){
            throw new MicroTimeManagementNotFoundException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
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
        User user = findById(userId);
        replaceRoleIdsWithNamesForUser(user);
        return userConverter.toDTO(user);
    }

    @Override
    public UserDTO findDTOByUsername(String username) {
        User user = findByUsername(username);
        replaceRoleIdsWithNamesForUser(user);
        return userConverter.toDTO(user);
    }

    @Override
    public UserDTO findDTOByEmail(String userEmail) {
        User user = findByEmail(userEmail);
        replaceRoleIdsWithNamesForUser(user);
        return userConverter.toDTO(user);
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

    public UserDTO saveUser(User user){
        log.info("Saving user: {}", user);
        return userConverter.toDTO(userRepository.save(user));
    }

    @Override
    public PaginationResultResponseDTO<UserDTO> getAllUsers(PageRequest pageRequest) {
        log.info("{}", pageRequest);
        Page<User> usersPage = userRepository.findAll(pageRequest);
        log.info("{}", usersPage.getTotalPages());
        log.info("{}", usersPage.hasContent());
        log.info("{}", usersPage.hasPrevious());
        log.info("{}", usersPage.hasNext());
        log.info("{}", usersPage.getNumber());
        // The persisted `roles` field holds role document IDs. Resolve to names
        // before responding so the admin UI can render and reason about them
        // without a second round-trip.
        List<UserDTO> payload = usersPage.getContent().stream()
                .peek(this::replaceRoleIdsWithNamesForUser)
                .map(userConverter::toDTO)
                .toList();
        return PaginationResultResponseDTO.<UserDTO>builder()
                .payload(payload)
                .pageSize(usersPage.getSize())
                .pageNumber(usersPage.getNumber())
                .sortedByFields(usersPage.getSort().get().map(Sort.Order::getProperty).collect(Collectors.toList()))
                .totalPages(usersPage.getTotalPages())
                .build();
    }

    @Override
    public List<UserDTO> modifyUserRoles(
            UsersRolesUpdateRequestDTO usersRolesUpdateRequestDTO,
            UserRoleUpdateAction updateAction
    ) {
        // Any of the three identifier lists may be null on the wire (only
        // `roleNames` is @NotEmpty). Treat null and empty alike, and de-dup so
        // a user supplied by both username and email isn't updated twice.
        List<User> userList = new ArrayList<>();
        List<String> userIds = nullSafe(usersRolesUpdateRequestDTO.getUserIds());
        List<String> emails = nullSafe(usersRolesUpdateRequestDTO.getEmails());
        List<String> usernames = nullSafe(usersRolesUpdateRequestDTO.getUsernames());

        if (!userIds.isEmpty()) {
            userList.addAll(userRepository.findByIdInAndIsActiveTrue(userIds));
        }
        if (!emails.isEmpty()) {
            userList.addAll(userRepository.findByEmailInAndIsActiveTrue(emails));
        }
        if (!usernames.isEmpty()) {
            userList.addAll(userRepository.findByUsernameInAndIsActiveTrue(usernames));
        }

        List<User> uniqueUsers = userList.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(User::getId, u -> u, (a, b) -> a, java.util.LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())));

        if (uniqueUsers.isEmpty()) {
            throw new MicroTimeManagementNotFoundException(ErrorConstants.USER_NOT_FOUND_IN_DB_RECORDS);
        }

        List<String> roleIds = roleService.findActiveRolesByName(usersRolesUpdateRequestDTO.getRoleNames())
                .stream().map(RoleDTO::getId).toList();

        if (roleIds.isEmpty()) {
            throw new MicroTimeManagementNotFoundException(ErrorConstants.ROLE_NOT_FOUND_ERROR);
        }

        uniqueUsers.forEach(user -> {
            // Always copy into a fresh LinkedHashSet — Mongo deserializes into a
            // mutable set in production, but tests (and other callers using the
            // User builder with `Set.of(...)`) can hand us an immutable one, and
            // we don't want this method to be coupled to that choice.
            java.util.Set<String> roles = user.getRoles() == null
                    ? new java.util.LinkedHashSet<>()
                    : new java.util.LinkedHashSet<>(user.getRoles());
            if (updateAction.equals(UserRoleUpdateAction.ADD)) {
                roles.addAll(roleIds);
            } else if (updateAction.equals(UserRoleUpdateAction.REMOVE)) {
                roles.removeAll(roleIds);
            }
            user.setRoles(roles);
        });
        // Flush the ID-role changes to the DB *before* the return path detaches
        // these entities to swap IDs for names — otherwise the detach would drop
        // the pending save.
        List<User> savedUsers = userRepository.saveAllAndFlush(uniqueUsers);
        // Resolve role IDs to names on the return path so the caller can render
        // them directly — same shape as /user/all.
        return savedUsers.stream()
                .peek(this::replaceRoleIdsWithNamesForUser)
                .map(userConverter::toDTO)
                .toList();
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

    @Override
    public UserDTO updateUserDetails(UserDetailsUpdateRequestDTO userDetailsUpdateRequestDTO) {
        // A user may only update their own profile. We trust the authenticated
        // principal as the source of truth and reject any request that points at a
        // different uid — this closes a horizontal-IDOR on /user/update.
        String authenticatedUid = currentAuthenticatedUid();
        if (authenticatedUid == null
                || !authenticatedUid.equals(userDetailsUpdateRequestDTO.getUid())) {
            throw new MicroTimeManagementUserException(ErrorConstants.CANNOT_UPDATE_UID_OF_EXISTING_USER);
        }

        User currentUser = userRepository.findByUidAndIsActiveTrue(userDetailsUpdateRequestDTO.getUid());

        if(null == currentUser){
            throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_USER_IDENTIFIER_VALUE);
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

        return userConverter.toDTO(saveUserEntity(currentUser));
    }

    private User saveUserEntity(User currentUser) {
        return userRepository.save(currentUser);
    }

    @Override
    public String deleteUserByUsername(String username) {
        User user = findByUsername(username);
        user.setIsActive(Boolean.FALSE);
        userRepository.save(user);
        return ResponseMessages.ACCOUNT_DELETED_SUCCESSFULLY;

    }

    private User findActiveUserByEmailOrUsername(String username){
        Optional<User> optionalUser = userRepository.findByEmailOrUsernameAndIsActiveTrue(username, username);
        if(optionalUser.isEmpty())
            throw new MicroTimeManagementNotFoundException(
                    String.format(ErrorConstants.ACCOUNT_NOT_FOUND_FOR_USERNAME, username));
        User user = optionalUser.get();
        return replaceRoleIdsWithNamesForUser(user);
    }
    @Override
    public String changeUserPassword(PasswordChangeRequestDTO passwordChangeRequestDTO) {
        User user = findActiveUserByEmailOrUsername(passwordChangeRequestDTO.getUsername());
        if(!bCryptPasswordEncoder.matches(passwordChangeRequestDTO.getOldPassword(), user.getPassword())){
            throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_PASSWORD_VALUE);
        }
        user.setPassword(bCryptPasswordEncoder.encode(passwordChangeRequestDTO.getNewPassword()));
        userRepository.save(user);
        return ResponseMessages.PASSWORD_CHANGED_SUCCESSFULLY;
    }

    private String currentAuthenticatedUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getUid();
        }
        return null;
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
        replaceRoleIdsWithNamesForUser(user);
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

    @Override
    public User replaceRoleIdsWithNamesForUser(User user) {
        if(null!=user){
            // This is an in-memory swap of persisted role IDs -> role-name
            // authorities (for Spring Security / DTO rendering). Detach first so
            // JPA never flushes the swapped names back over the stored IDs and
            // corrupts them. No-op outside a persistence context (unit tests).
            if (entityManager != null) {
                entityManager.detach(user);
            }
            user.setRoles(roleService.getRoleNamesForIds(user.getRoles()));
            return user;
        }
        return null;
    }

}
