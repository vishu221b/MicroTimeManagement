package com.microtimemanagement.apiservice;

import com.microtimemanagement.apiservice.model.Role;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.repository.RoleRepository;
import com.microtimemanagement.apiservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CliRunner implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void run(String... args) throws Exception {
        List<Role> roles = new ArrayList<>();
        /*
         * ROLE_ is appended by default when the securityFilterChain checks for Role
         */
        if(null==roleRepository.findByName("MTM_USER")){
            roles.add(Role.builder().name("MTM_USER").build());
        }
        if(null==roleRepository.findByName("MTM_ADMIN")){
            roles.add(Role.builder().name("MTM_ADMIN").build());
        }

        if(!roles.isEmpty())
            roleRepository.saveAll(roles);

        if (userRepository.findByUsername("mtm_admin").isEmpty())
            userRepository.save(
                    User.builder()
                            .email("admin@mtm.com")
                            .username("mtm_admin")
                            .password(bCryptPasswordEncoder.encode("mtm_password"))
                            .firstName("Mtm Admin")
                            .roles(roles.stream().map(Role::getId).collect(Collectors.toSet()))
                            .build()
            );
    }
}
