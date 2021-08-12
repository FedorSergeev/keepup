package io.keepup.cms.core.datasource.dao.sql;

import io.keepup.cms.core.datasource.dao.FileDao;
import io.keepup.cms.core.datasource.sql.entity.FileEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveFileRepository;
import io.keepup.cms.core.persistence.FileWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Implementation of data access objects for files and relation databases
 *
 * @author Fedor Sergeev
 * @since 2.0
 */
@Service
public class SqlFileDao implements FileDao {

    private static final String KEEPUP_STORAGE_USER_DIRECTORY_PATH_APP_FILES = "${keepup.storage.user-directory-path:/app/files}";
    private static final String SUFFIX = "/";

    private final Log log = LogFactory.getLog(getClass());
    private final ReactiveFileRepository fileRepository;

    @Value(KEEPUP_STORAGE_USER_DIRECTORY_PATH_APP_FILES)
    private String userFileDirectoryPath;

    public SqlFileDao(ReactiveFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // region public API

    /**
     * Tries to fetch the file as {@link OutputStream} from entity in database. Though files table stores information
     * about files, this method returns object of another type as there is no need to save {@link File} in
     * local filesystem where the application is being hosted. You can easily create the file from
     * resulting {@link OutputStream} insode of your business-logic
     *
     * @param fileName name of the file
     * @return Publisher for the {@link OutputStream} object or empty Mono if there was no file and no content entity attribute
     */
    @Override
    public Mono<OutputStream> getFileAsStream(String fileName) {
        return fileRepository.findByFilenameAndPath(fileName, userFileDirectoryPath)
                .map(this::getOutputStream)
                .flatMap(Mono::justOrEmpty);
    }

    @Override
    public Mono<FileWrapper> getFile(final String fileName) {
        return fileRepository.findByFilenameAndPath(fileName, userFileDirectoryPath)
                .map(fileEntity -> getFileWrapper(fileName, userFileDirectoryPath, fileEntity))
                .flatMap(Mono::justOrEmpty);
    }

    // endregion

    @NotNull
    private FileWrapper getFileWrapper(String fileName, String path, FileEntity fileEntity) {

        String finalPath = path.endsWith(SUFFIX)
                ? path.concat(SUFFIX)
                : path;
        var fileWrapper = new FileWrapper();
        fileWrapper.setId(fileEntity.getId());
        fileWrapper.setName(fileName);
        fileWrapper.setPath(finalPath);
        fileWrapper.setExists(true);
        fileWrapper.setContent(getOutputStream(fileEntity).orElse(null));
        fileWrapper.setCreationDate(fileEntity.getCreationTime());
        fileWrapper.setLastModified(fileEntity.getModificationTime());
        return fileWrapper;
    }

    private Optional<OutputStream> getOutputStream(FileEntity fileEntity) {
        if (fileEntity == null) {
            log.error("No file attribute was found");
            return Optional.empty();
        }
        return createNewOutputStreamFromDatabase(fileEntity);
    }

    private Optional<OutputStream> createNewOutputStreamFromDatabase(FileEntity fileEntity) {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(fileEntity.getContent());
            return Optional.of(byteArrayOutputStream);
        } catch (IOException ex) {
            log.error(format("Could not create file output stream '%s' because if IOException: %s. empty Mono will be returned.",
                    fileEntity.getFileName(), ex.getMessage()));
            return Optional.empty();
        }
    }
}
