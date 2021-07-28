package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.NodeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactiveNodeEntityRepository extends ReactiveCrudRepository<NodeEntity, Long> {
}
