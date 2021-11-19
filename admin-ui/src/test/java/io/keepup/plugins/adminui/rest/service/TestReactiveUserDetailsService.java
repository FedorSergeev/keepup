package io.keepup.plugins.adminui.rest.service;

import io.keepup.cms.core.persistence.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;

@Service("testReactiveUserDetailsService")
public class TestReactiveUserDetailsService implements ReactiveUserDetailsService {
    private final Log log = LogFactory.getLog(getClass());
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        log.info("Fetching user object with name = %s".formatted(username));
        if ("empty".equals(username)) {
            return Mono.empty();
        }
        User user = new User();
        user.setEnabled(true);
        user.setUsername(username);
        user.setPassword("somePass");
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_MOCK")));
        user.setExpirationDate(LocalDate.MAX);
        user.setAdditionalInfo("{}");
        user.setId(1L);
        return Mono.just(user);
    }
}
