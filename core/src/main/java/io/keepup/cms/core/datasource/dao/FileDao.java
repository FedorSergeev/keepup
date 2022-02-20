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
    /**
     * Get an output stream of bytes from file.
     *
     * @param fileName name of file
     * @return         an output stream of bytes
     */
    Mono<OutputStream> getFileAsStream(String fileName);

    /**
     * Get meta information about file.
     *
     * @param filename name of file
     * @return         meta information about file
     * @see FileWrapper
     */
    Mono<FileWrapper> getFile(String filename);
}
