package io.keepup.cms.rest.controller;

import io.keepup.cms.core.service.AbstractEntityOperationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WrongAbstractEntityService extends AbstractEntityOperationService<SomeEntity> {
    @Override
    public Flux<SomeEntity> getAll() {
        return Flux.error(new RuntimeException("Wrong entity service getAll method invoked"));
    }

    @Override
    public Mono<SomeEntity> get(Long id) {
        return Mono.error(new RuntimeException("Wrong entity service get method invoked"));
    }
}
