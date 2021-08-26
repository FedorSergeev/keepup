package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access object for application users
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
public interface UserDao {
    Mono<User> saveUser(User user);
    Mono<User> getUser(long userId);
    Flux<User> getUsers(Iterable<String> roles);
    Mono<Void> deleteUser(long id);
    Mono<UserDetails> getByName(String username);
}
