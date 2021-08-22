package io.keepup.cms.core.datasource.resources;

import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.commons.ValidationResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.keepup.cms.core.datasource.resources.FtpUtils.SLASH;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

@Service
public class FtpFileProcessor implements StorageAccessor<String> {

    private final Log log = LogFactory.getLog(getClass());

    private final String username;
    private final String password;
    private final String server;
    private final int port;
    private final String dump;

    public FtpFileProcessor(ApplicationConfig applicationConfig) {
        username = applicationConfig.getUsername();
        password = applicationConfig.getPassword();
        server = applicationConfig.getServer();
        port = applicationConfig.getPort();
        dump = applicationConfig.getDump();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TransferOperationResult<String> save(File file, String relativePath) {
        return new FtpOperationExecutor<String>(username, password, server, port).doFtpOperation(file, singletonList(relativePath), (ftpClient, array) -> {
            var validationResult = validateSaveFileArguments(array);
            if (validationResult.isSuccess()) {
                var fileToUpload = (File)array[0];
                final List<String> paths = (List<String>)array[1];
                var path = paths.isEmpty() ? SLASH : paths.get(0);
                try (var fileInputStream = new FileInputStream(fileToUpload)) {
                    makeDirectories(ftpClient, path);
                    final String fileToUploadName = fileToUpload.getName();
                    stream(ofNullable(ftpClient.listFiles()).orElse(new FTPFile[0]))
                          .map(FTPFile::getName)
                          .filter(fileToUploadName::equals)
                          .forEach(name -> deleteFile(name, ftpClient));
                    return ftpClient.appendFile(new String(fileToUploadName.getBytes(UTF_8), UTF_8), fileInputStream)
                            ? new TransferOperationResult<String>().ok()
                            : new TransferOperationResult<String>().error("Failed to upload file");
                } catch (FileNotFoundException e) {
                    log.error("File %s not found".formatted(file.getName()));
                    return new TransferOperationResult<String>().error(e.toString());
                } catch (IOException ex) {
                    log.error("Failed to save file %s: %s".formatted(file.getName(), ex.toString()));
                    return new TransferOperationResult<String>().error(ex.toString());
                }
            } else return new TransferOperationResult<String>().error(validationResult.getMessage());
        });
    }

    @Override
    public GetTreeFromStoreResult getByType(String type, String... relativePaths) {
        final List<String> pathsList = ofNullable(relativePaths).map(Arrays::asList).orElse(Collections.emptyList());
        final TransferOperationResult<List<StoredFileData>> transferOperationResult
                = new FtpOperationExecutor<List<StoredFileData>>(username, password, server, port)
                .doFtpOperation(null, pathsList, new GetFilesByTypeOperation(type, dump));
        var result = new GetTreeFromStoreResult();
        result.setSuccess(transferOperationResult.isSuccess());
        result.setFiles(transferOperationResult.getPayload());
        result.setMessage(transferOperationResult.getMessage());
        return result;
    }

    /**
     * Actually invokes {@link FtpFileProcessor#get(String)} method and reduces result to one file
     *
     * @param name         filename file to look for
     * @param relativePath root directory
     * @return             operation result wrapper and error message if no file was found
     */
    @Override
    public GetFileFromStoreResult getByName(String name, String relativePath) {
        if (!relativePath.endsWith(SLASH)) {
            relativePath = relativePath.concat(SLASH);
        }
        final var getTreeFromStoreResult = get(relativePath.concat(name));
        var result = new GetFileFromStoreResult();
        if (getTreeFromStoreResult.getFiles().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("No file %s found in %s".formatted(name, relativePath));
        } else {
            result.setSuccess(getTreeFromStoreResult.isSuccess());
            result.setMessage(getTreeFromStoreResult.getMessage());
            result.setFile(getTreeFromStoreResult.getFiles().get(0));
        }
        return result;
    }

    private void deleteFile(String filename, FTPClient ftpClient) {
        try {
            ftpClient.deleteFile(filename);
        } catch (IOException e) {
            log.error("Failed to delete file %s: %s".formatted(filename, e.toString()));
        }
    }

    private ValidationResult validateSaveFileArguments(Object[] array) {
        if (array.length != 2) {
            return ValidationResult.error("wrong arguments");
        }
        if (!(array[0] instanceof File)) {
            return ValidationResult.error("wrong file argument");
        }
        if (!(array[1] instanceof List)) {
            return ValidationResult.error("wrong path argument");
        }
        return ValidationResult.success();
    }

    private void makeDirectories(FTPClient ftpClient, String dirPath) throws IOException {
        if (dirPath.isEmpty()) {
            return;
        }
        String[] pathElements = dirPath.split(SLASH);

        for (String singleDir : pathElements) {
            boolean existed = ftpClient.changeWorkingDirectory(singleDir);
            if (!existed) {
                boolean created = ftpClient.makeDirectory(singleDir);
                if (created) {
                    log.info("CREATED directory: %s".formatted(singleDir));
                    ftpClient.changeWorkingDirectory(singleDir);
                } else {
                    log.error("FAILED to create directory: %s".formatted(singleDir));
                    return;
                }
            }
        }

    }

    private GetTreeFromStoreResult get(String relativePath) {
        final var transferOperationResult
                = new FtpOperationExecutor<List<StoredFileData>>(username, password, server, port)
                .doFtpOperation(null, singletonList(relativePath), new GetFilesOperation(relativePath, dump));
        var result = new GetTreeFromStoreResult();
        result.setSuccess(transferOperationResult.isSuccess());
        result.setFiles(transferOperationResult.getPayload());
        result.setMessage(transferOperationResult.getMessage());
        return result;
    }
}
