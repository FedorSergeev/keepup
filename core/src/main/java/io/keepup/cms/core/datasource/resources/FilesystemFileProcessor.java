package io.keepup.cms.core.datasource.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;

/**
 * Component for working wih static content right on the server side in the same filesystem.
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public class FilesystemFileProcessor implements StorageAccessor<String> {

    private static final String SLASH = "/";
    private final Log log = LogFactory.getLog(getClass());

    private final String rootPath;
    private final String staticPath;
    private final String appDocumentRoot;

    /**
     * Constructs files accessor with the specified root lookup directory
     * @param rootPath root directory address
     * @param documentRoot document root for application
     * @param staticPath path to static resources
     */
    public FilesystemFileProcessor(final String rootPath, final String documentRoot, final String staticPath) {
        this.rootPath = rootPath;
        this.appDocumentRoot = documentRoot;
        this.staticPath = staticPath;
    }

    /**
     * Stores the specified file in path relative to storage root
     *
     * @param file         file to be saved
     * @param relativePath relative path to file
     * @return             result wrapper with success marker and error message in negative cases
     */
    @Override
    public TransferOperationResult<String> save(final File file, final String relativePath) {
        TransferOperationResult<String> transferOperationResult;
        final String filePath = appDocumentRoot.concat(relativePath);
        try {
            copyFileToDirectory(file, new File(filePath));
            log.info(format("File %s saved to directory %s", file.getName(), filePath));
            transferOperationResult = new TransferOperationResult<String>().ok();
        } catch (IOException ex) {
            log.error(format("Failed to save file %s to dump directory: %s", file.getName(), ex.getMessage()));
            transferOperationResult = new TransferOperationResult<String>().error(ex.toString());
        }
        return transferOperationResult;
    }

    /**
     * Searches for the file with the specified extension and relative to storage root path
     *
     * @param type          file extension
     * @param relativePaths directories to search for files
     * @return              resulting file wrapper or error message if there was o file found
     *                      or it is a directory
     */
    @Override
    public GetTreeFromStoreResult getByType(final String type, final String... relativePaths) {
        if (relativePaths == null) {
            return GetTreeFromStoreResult.error("No relative paths specified");
        }

        final List<StoredFileData> resultFiles = new ArrayList<>();

        for (final var path : relativePaths) {
            final var normalizedPath = path.toLowerCase();
            final var baseHtmlDirectory = new File(staticPath.concat(normalizedPath));
            final var directoryListing = baseHtmlDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".".concat(type)));
            ofNullable(directoryListing)
                    .ifPresent(files -> Arrays.stream(files)
                                              .forEach((file -> resultFiles.add(new StoredFileData(file, normalizedPath)))));
        }
        final var result = new GetTreeFromStoreResult();
        result.setSuccess(true);
        result.setFiles(resultFiles);
        return result;
    }

    /**
     * Searches for the specified by name and relative to storage root path.
     *
     * @param name         filename ame of file
     * @param relativePath root directory
     * @return             wrapped result with message and success flag
     */
    @Override
    public GetFileFromStoreResult getByName(final String name, final String relativePath) {
        final var directory = new File(rootPath.concat(relativePath));
        if (directory.exists() && directory.isDirectory()) {
            final var resultFile = new File(directory.getAbsolutePath().concat(SLASH).concat(name));
            if (resultFile.exists() && !resultFile.isDirectory()) {
                final var fileData = new StoredFileData(resultFile, relativePath);
                final var result = new GetFileFromStoreResult();
                result.setFile(fileData);
                result.setSuccess(true);
                return result;
            }
            return GetFileFromStoreResult.error(format("Requested file does not exist or is a directory: %s%s%s", relativePath, SLASH, name));
        }
        return GetFileFromStoreResult.error(format("Wrong path to file: %s", relativePath));
    }
}
