package io.keepup.plugins.adminui.rest.controller;

import io.keepup.cms.core.persistence.User;
import io.keepup.plugins.adminui.rest.model.UserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import static java.util.Optional.ofNullable;

/**
 * Controller with the set of REST endpoints serving administrative UI panel based on
 * different frontend solutions
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
@RestController
@RequestMapping("admin-ui")
public class AdminPanelController {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Fetch information about user
     *
     * @param session server session attributes
     * @return        information about currently logged in user or stub if user is not authenticated.
     */
    @GetMapping("/userinfo")
    public Mono<ResponseEntity<UserInfo>> getUserInfo(WebSession session) {
        log.info("Session id: %s, User information requested".formatted(session.getId()));
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .flatMap(this::getUser)
                .map(this::getUserInfo)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.ok(UserInfo.empty())))
                .doOnNext(response -> log.info("Session id: %s, Sending response with status %s: %s"
                        .formatted(session.getId(),
                                   response.getStatusCode().toString(),
                                   ofNullable(response.getBody()).map(UserInfo::toString).orElse("null}"))));
    }

    private Mono<User> getUser(Object principal) {
        if (principal instanceof User) {
            return Mono.just(User.class.cast(principal));
        }
        return Mono.empty();
    }

    private UserInfo getUserInfo(User user) {
        var userInfo = new UserInfo();
        userInfo.setName(user.getUsername());
        userInfo.setPicture("assets/images/default.png");
        return userInfo;
    }
}