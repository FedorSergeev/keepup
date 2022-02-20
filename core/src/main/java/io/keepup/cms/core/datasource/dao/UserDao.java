package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Data access object for application users
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public interface UserDao {
    /**
     * Save user in data source.
     *
     * @param user user ti be saved
     * @return     saved user instance
     */
    Mono<User> saveUser(User user);

    /**
     * Find user by his ID.
     *
     * @param userId user's primary identifier
     * @return       reactor.core.publisher.Mono emitting the found user
     */
    Mono<User> getUser(long userId);

    /**
     * Find all users by role.
     *
     * @param roles collection of role names
     * @return      reactive stream publisher emitting al the found users who have at least one of the specified roles
     */
    Flux<User> getUsers(Iterable<String> roles);

    /**
     * Delete user.
     *
     * @param id user's ID
     * @return   Publisher emitting {@link Void} when the user is deleted
     */
    Mono<Void> deleteUser(long id);

    /**
     * Find {@link UserDetails} object by username.
     *
     * @param username name of user.
     * @return         reactor.core.publisher.Mono emitting the user description
     */
    Mono<UserDetails> getByName(String username);
}
