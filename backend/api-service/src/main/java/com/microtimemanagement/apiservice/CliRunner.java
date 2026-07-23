package com.microtimemanagement.apiservice;

import com.microtimemanagement.apiservice.constants.RoleConstants;
import com.microtimemanagement.apiservice.model.Role;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CliRunner implements CommandLineRunner {

    private static final List<String> SEED_ROLE_NAMES = List.of(
            RoleConstants.USER_OPS_ROLE_WITH_PREFIX,
            RoleConstants.ADMIN_OPS_ROLE_WITH_PREFIX,
            RoleConstants.ROLE_CRUD_WITH_PREFIX,
            RoleConstants.ACTIVITY_CRUD_WITH_PREFIX
    );

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Ensure every seed role exists (create only the missing ones).
        List<Role> toCreate = new ArrayList<>();
        for (String name : SEED_ROLE_NAMES) {
            if (roleRepository.findByName(name) == null) {
                toCreate.add(Role.builder().name(name).build());
            }
        }
        if (!toCreate.isEmpty()) {
            roleRepository.saveAll(toCreate);
        }

        // Resolve the full seed-role id set from the DB (NOT just the ones we
        // created this run) — otherwise, on a DB where the roles already exist
        // but the admin user doesn't, the bootstrap admin would be created with
        // no roles and be unable to reach any admin feature.
        Set<String> seedRoleIds = SEED_ROLE_NAMES.stream()
                .map(roleRepository::findByName)
                .filter(java.util.Objects::nonNull)
                .map(Role::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Optional<User> existingAdmin = userRepository.findByUsername("mtm_admin");
        if (existingAdmin.isEmpty()) {
            userRepository.save(
                    User.builder()
                            .email("admin@mtm.com")
                            .username("mtm_admin")
                            .password(bCryptPasswordEncoder.encode("mtm_password"))
                            .firstName("Mtm Admin")
                            .roles(seedRoleIds)
                            .build()
            );
        } else {
            // Self-heal: guarantee the bootstrap admin always carries the seed
            // roles (e.g. if it was previously created before they existed).
            User admin = existingAdmin.get();
            Set<String> current = admin.getRoles() == null
                    ? new LinkedHashSet<>()
                    : new LinkedHashSet<>(admin.getRoles());
            if (current.addAll(seedRoleIds)) {
                admin.setRoles(current);
                userRepository.save(admin);
            }
        }
    }
}
