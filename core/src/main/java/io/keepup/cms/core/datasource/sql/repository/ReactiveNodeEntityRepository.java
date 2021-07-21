package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactiveNodeEntityRepository extends ReactiveCrudRepository<NodeEntity, Long> {
}
