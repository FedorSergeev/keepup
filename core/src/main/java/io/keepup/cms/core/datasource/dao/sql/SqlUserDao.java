package io.keepup.cms.core.datasource.dao.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.datasource.dao.UserDao;
import io.keepup.cms.core.datasource.sql.EntityUtils;
import io.keepup.cms.core.datasource.sql.entity.RoleByUserIdEntity;
import io.keepup.cms.core.datasource.sql.entity.RoleEntity;
import io.keepup.cms.core.datasource.sql.entity.UserAttributeEntity;
import io.keepup.cms.core.datasource.sql.entity.UserEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveRoleByUserEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.cms.core.persistence.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of data access objects for {@link io.keepup.cms.core.persistence.User} and relation databases
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@Service
public class SqlUserDao implements UserDao {

    private final Log log = LogFactory.getLog(getClass());

    private final ObjectMapper mapper;
    private final ReactiveUserEntityRepository userEntityRepository;
    private final ReactiveUserAttributeEntityRepository userAttributeEntityRepository;
    private final ReactiveRoleByUserEntityRepository roleByUserEntityRepository;

    @Autowired
    public SqlUserDao(ObjectMapper objectMapper,
                      ReactiveUserEntityRepository userEntityRepository,
                      ReactiveUserAttributeEntityRepository userAttributeEntityRepository,
                      ReactiveRoleByUserEntityRepository reactiveRoleByUserEntityRepository) {
        this.mapper = objectMapper;
        this.userEntityRepository = userEntityRepository;
        this.userAttributeEntityRepository = userAttributeEntityRepository;
        this.roleByUserEntityRepository = reactiveRoleByUserEntityRepository;
        log.debug("RDB DAO for users instantiated");
    }

    // region public API

    /**
     * Creates information from {@link User} object into referring database tables. Does not save roles
     *
     * @param user application user
     * @return User object built from the saved information including identifier
     */
    @Override
    public Mono<User> saveUser(User user) {
        return ofNullable(user).map(userDto -> userEntityRepository.save(getUserEntity(userDto))
                .flatMap(saved -> {
                    userDto.setId(saved.getId());
                    return userAttributeEntityRepository.saveAll(getAttributeEntities(saved.getId(), userDto.getAttributes()))
                            .collect(toList());
                })
                .flatMap(userAttributeEntities -> {
                    userDto.setAttributes(getAttributes(userAttributeEntities));
                    return roleByUserEntityRepository.saveAll(getRoles(userDto.getId(), userDto.getAuthorities())).collect(toList());
                })
                .map(roles -> {
                    userDto.setAuthorities(getGrantedAuthorities(roles.stream().map(RoleByUserIdEntity::getRole).collect(Collectors.toList())));
                    return userDto;
                }))
                .orElseGet(Mono::empty);
    }


    private Iterable<RoleByUserIdEntity> getRoles(Long userId, Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(authority -> {
            var roleByUserIdEntity = new RoleByUserIdEntity();
            roleByUserIdEntity.setUserId(userId);
            roleByUserIdEntity.setRole(authority.getAuthority());
            return roleByUserIdEntity;
        }).collect(Collectors.toList());
    }

    /**
     * Finds user by id
     *
     * @param userId user identifier
     * @return application user object
     */
    @Override
    public Mono<User> getUser(long userId) {
        return userEntityRepository.findById(userId)
                .flatMap(userEntity -> userAttributeEntityRepository.findAllByUserId(userId).collect(toList())
                        .map(userAttributeEntities -> buildUser(userEntity, userAttributeEntities)))
                .flatMap(user -> roleByUserEntityRepository.findRolesByUserId(user.getId())
                        .collect(toList())
                        .map(roles -> getUserWithRoles(user, roles)));
    }

    /**
     * Finds all users with the specified roles
     *
     * @param roles specified roles that users should have or null if there is a need to get all users
     * @return users Flux
     */
    @Override
    public Flux<User> getUsers(Iterable<String> roles) {
        return ofNullable(roles)
                .map(userRoles -> roleByUserEntityRepository.findAllWhoHasRoles(userRoles)
                        .collect(Collectors.groupingBy(RoleByUserIdEntity::getUserId, toList()))
                        .flatMapMany(userRoleEntities -> userEntityRepository.findAllById(userRoleEntities.keySet())
                                .flatMap(userEntity -> userAttributeEntityRepository.findAllByUserId(userEntity.getId()).collect(toList())
                                        .map(userAttributeEntities -> buildUser(userEntity, getUserRoles(userRoleEntities.get(userEntity.getId())), userAttributeEntities)))))
                .orElse(roleByUserEntityRepository.findAll()
                        .collect(Collectors.groupingBy(RoleByUserIdEntity::getUserId, toList()))
                        .flatMapMany(userRoleEntities -> userEntityRepository.findAllById(userRoleEntities.keySet())
                                .flatMap(userEntity -> userAttributeEntityRepository.findAllByUserId(userEntity.getId()).collect(toList())
                                        .map(userAttributeEntities -> buildUser(userEntity, getUserRoles(userRoleEntities.get(userEntity.getId())), userAttributeEntities)))));
    }

    @Override
    public Mono<Void> deleteUser(long id) {
        return userEntityRepository.deleteById(id);
    }

    // endregion

    Iterable<String> getUserRoles(List<RoleByUserIdEntity> roleEntities) {
        return ofNullable(roleEntities)
                .map(roles -> roles.stream()
                        .map(RoleByUserIdEntity::getRole)
                        .collect(toList()))
                .orElse(emptyList());
    }

    User buildUser(UserEntity userEntity, List<UserAttributeEntity> attributeEntities) {
        var user = new User();
        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPasswordHash());
        user.setAdditionalInfo(userEntity.getAdditionalInfo());
        user.setAttributes(getAttributes(attributeEntities));
        user.setId(userEntity.getId());
        user.setExpirationDate(userEntity.getExpirationDate());
        user.setEnabled(userEntity.getExpirationDate()
                .isAfter(EntityUtils.convertToLocalDateViaInstant(new Date())));
        log.debug("User object created from database entity with id = %d".formatted(user.getId()));
        return user;
    }

    User buildUser(UserEntity userEntity, Iterable<String> roles, List<UserAttributeEntity> attributeEntities) {
        var user = buildUser(userEntity, attributeEntities);
        user.setAuthorities(getGrantedAuthorities(roles));
        return user;
    }

    @NotNull
    private Collection<GrantedAuthority> getGrantedAuthorities(Iterable<String> roles) {
        List<GrantedAuthority> userAuthorities = new ArrayList<>();
        roles.forEach(role -> userAuthorities.add(new SimpleGrantedAuthority(role)));
        return userAuthorities;
    }

    private UserEntity getUserEntity(User user) {
        long userId = user.getId();
        var userEntity = new UserEntity();
        userEntity.setId(userId == 0 ? null : userId);
        userEntity.setPasswordHash(user.getPassword());
        userEntity.setAdditionalInfo(user.getAdditionalInfo());
        userEntity.setUsername(user.getUsername());
        userEntity.setExpirationDate(user.getExpirationDate());
        return userEntity;
    }

    private Map<String, Serializable> getAttributes(List<UserAttributeEntity> attributeEntities) {
        final Map<String, Serializable> attributes = new HashMap<>();
        attributeEntities.forEach(attributeEntity -> attributes.put(attributeEntity.getAttributeKey(), getUserAttribute(attributeEntity)));
        return attributes;
    }

    private Serializable getUserAttribute(UserAttributeEntity userAttributeEntity) {
        try {
            Class<?> attributeType = Class.forName(userAttributeEntity.getJavaClass());
            if (List.class.isAssignableFrom(attributeType) || userAttributeEntity.getJavaClass().contains("$ArrayList")) {
                userAttributeEntity.setJavaClass(ArrayList.class.getName());
            }
            return (Serializable) mapper.readValue(userAttributeEntity.getAttributeValue(), attributeType);
        } catch (IOException ex) {
            log.error("Failed to serialize value from persistent content: %s".formatted(ex));
        } catch (ClassNotFoundException e) {
            log.error("Class %s not found in classpath: %s".formatted(userAttributeEntity.getJavaClass(), e.getMessage()));
        }
        return null;
    }

    private List<UserAttributeEntity> getAttributeEntities(final Long userId, Map<String, Serializable> attributes) {
        if (userId == null) {
            log.warn("Null user id was passed for saving user's attribute");
            return emptyList();
        }
        if (attributes == null) {
            log.warn("Null attributes were passed to be saved for user");
            return emptyList();
        }

        return attributes.entrySet()
                .stream()
                .map(entry -> new UserAttributeEntity(userId, entry.getKey(), entry.getValue()))
                .collect(toList());
    }

    @NotNull
    private User getUserWithRoles(User user, List<RoleEntity> roles) {
        user.setAuthorities(getGrantedAuthorities(roles.stream().map(RoleEntity::getRole).collect(toList())));
        return user;
    }

}
