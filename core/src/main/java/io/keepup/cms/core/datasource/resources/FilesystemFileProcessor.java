package io.keepup.cms.core.datasource.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;

/**
 * Component for working wih static content right on the server side in the same filesystem
 */
public class FilesystemFileProcessor implements StorageAccessor<String> {

    public static final String SLASH = "/";
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
    public FilesystemFileProcessor(String rootPath, String documentRoot, String staticPath) {
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
    public TransferOperationResult<String> save(File file, String relativePath) {
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
    public GetTreeFromStoreResult getByType(String type, String... relativePaths) {
        if (relativePaths == null) {
            return GetTreeFromStoreResult.error("No relative paths specified");
        }

        final List<StoredFileData> resultFiles = new ArrayList<>();

        for (String path : relativePaths) {
            String normalizedPath = path.toLowerCase();
            File baseHtmlDirectory = new File(staticPath.concat(normalizedPath));
            File[] directoryListing = baseHtmlDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".".concat(type)));
            ofNullable(directoryListing)
                    .ifPresent(files -> Arrays.stream(files)
                                              .forEach((file -> resultFiles.add(new StoredFileData(file, normalizedPath)))));
        }
        GetTreeFromStoreResult result = new GetTreeFromStoreResult();
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
    public GetFileFromStoreResult getByName(String name, String relativePath) {
        final File directory = new File(rootPath.concat(relativePath));
        if (directory.exists() && directory.isDirectory()) {
            final File resultFile = new File(directory.getAbsolutePath().concat(SLASH).concat(name));
            if (resultFile.exists() && !resultFile.isDirectory()) {
                final StoredFileData fileData = new StoredFileData(resultFile, relativePath);
                final GetFileFromStoreResult result = new GetFileFromStoreResult();
                result.setFile(fileData);
                result.setSuccess(true);
                return result;
            }
            return GetFileFromStoreResult.error(format("Requested file does not exist or is a directory: %s%s%s", relativePath, SLASH, name));
        }
        return GetFileFromStoreResult.error(format("Wrong path to file: %s", relativePath));
    }
}
