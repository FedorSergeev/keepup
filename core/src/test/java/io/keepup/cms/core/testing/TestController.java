package io.keepup.cms.core.testing;

import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for testing purposes
 */
@RestController
@RequestMapping("/testing")
public class TestController {

    @Autowired
    private DataSourceFacade dataSourceFacade;

    @GetMapping
    public Mono<ResponseEntity> test() {
        return Mono.just(ResponseEntity.ok("ok"));
    }

    @PreAuthorize("hasRole('user')")
    @GetMapping("/with-role")
    public Mono<ResponseEntity<String>> testingWithRole() {
        return Mono.just(ResponseEntity.ok("okay"));
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/with-role-admin")
    public Mono<ResponseEntity<String>> testingWithAdminRole() {
        return Mono.just(ResponseEntity.ok("okay"));
    }
}
