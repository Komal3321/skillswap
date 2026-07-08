package com.skillswap.config;

import java.util.Arrays;

import com.skillswap.domain.entity.Role;
import com.skillswap.domain.entity.Role.RoleName;
import com.skillswap.repository.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensures required application roles exist for authentication flows.
 */
@Configuration
public class RoleBootstrapConfiguration {

    /**
     * Inserts missing role records at startup.
     *
     * @param roleRepository role repository
     * @return startup runner
     */
    @Bean
    public ApplicationRunner roleBootstrapRunner(RoleRepository roleRepository) {
        return args -> Arrays.stream(RoleName.values())
                .filter(roleName -> roleRepository.findByName(roleName).isEmpty())
                .map(roleName -> Role.builder().name(roleName).build())
                .forEach(roleRepository::save);
    }
}
