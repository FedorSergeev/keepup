package io.keepup.cms.core.datasource.sql.repository;

import io.keepup.cms.core.datasource.sql.entity.FileEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * DAO for files stored in the database
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public interface ReactiveFileRepository extends ReactiveCrudRepository<FileEntity, Long> {

    /**
     * Find a file by name and logical path.
     *
     * @param filename name of file
     * @param path     logical path to file
     * @return         Mono emitting the needed file
     */
    @Query("SELECT id, file_name, path, content_id, content, creation_time, modification_time FROM files " +
            "AS file " +
            "WHERE file.file_name  IN (:filename) " +
            "AND file.path IN (:path)")
    Mono<FileEntity> findByFilenameAndPath(@Param("filename") String filename, @Param("path") String path);

}
