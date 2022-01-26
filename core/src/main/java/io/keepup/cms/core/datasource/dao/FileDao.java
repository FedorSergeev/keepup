package io.keepup.cms.core.datasource.dao;

import io.keepup.cms.core.persistence.FileWrapper;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

/**
 * Data access object for files
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public interface FileDao {
    Mono<OutputStream> getFileAsStream(String fileName);
    Mono<FileWrapper> getFile(String filename);
}
