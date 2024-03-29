package io.keepup.cms.core.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AbstractEntityWithFinalFieldOperationService extends AbstractEntityOperationService<TestEntityWithFinalField> {

    @Override
    public Mono<TestEntityWithFinalField> save(TestEntityWithFinalField entity, long ownerId) {
        return super.save(entity, ownerId);
    }
}
