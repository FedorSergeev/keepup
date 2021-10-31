package io.keepup.plugins.adminui.rest.controller;

import io.keepup.cms.core.persistence.User;
import io.keepup.plugins.adminui.rest.model.UserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller with the set of REST endpoints serving administrative UI panel based on
 * different frontend solutions
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@RestController
@RequestMapping("admin-ui")
public class AdminPanelController {
    @GetMapping("/userinfo")
    public Mono<ResponseEntity<UserInfo>> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .map(User.class::cast)
                .map(this::getUserInfo)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.ok(UserInfo.empty())));
    }

    private UserInfo getUserInfo(User user) {
        var userInfo = new UserInfo();
        userInfo.setName(user.getUsername());
        userInfo.setPicture("assets/images/default.png");
        return userInfo;
    }
}