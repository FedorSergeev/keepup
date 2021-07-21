package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.Content;
import reactor.core.publisher.Mono;

/**
 * Interface for the basic DAO entity.
 *
 * @author Fedor Sergeev f.sergeev@trans-it.pro
 */
public interface DataSource {

    Mono<Content> getContent(Long id);
    Mono<Long> createContent(Content content);

}
