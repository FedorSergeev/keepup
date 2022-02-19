package io.keepup.cms.core.datasource.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;

/**
 * FTP operation or getting list of files from remote directory recursively
 */
public class GetFilesByTypeOperation implements FtpOperation<List<StoredFileData>> {
    /**
     * Slash symbol
     */
    public static final String SLASH_SYMBOL = "/";

    private final Log log = LogFactory.getLog(getClass());
    private final String type;
    private final String localFilePath;

    /**
     * Wrapper for getting files by file type operation.
     *
     * @param type     file type
     * @param filePath logical path to file
     */
    public GetFilesByTypeOperation(String type, String filePath) {
        localFilePath = "%s/%s".formatted(filePath, "ftp/");
        final var localFileDirectory = new File(localFilePath);
        this.type = type;
        if (!localFileDirectory.exists() && !localFileDirectory.mkdir()) {
            log.error(format("Failed to create directory %s", localFileDirectory.getAbsolutePath()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public TransferOperationResult<List<StoredFileData>> apply(FTPClient ftpClient, Object... parameters) {
        List<String> currentRelativePaths = parameters == null || parameters.length == 0 || !(parameters[parameters.length - 1] instanceof List)
                ? Collections.emptyList()
                : (List<String>)parameters[parameters.length - 1];
        if (currentRelativePaths == null || currentRelativePaths.isEmpty()) {
            return new TransferOperationResult<List<StoredFileData>>().error("Empty relative paths specified in request");
        }

        final TransferOperationResult<List<StoredFileData>> operationResult = new TransferOperationResult<>();
        operationResult.setPayload(new ArrayList<>());
        List<StoredFileData> files = new ArrayList<>();

        currentRelativePaths.forEach(path -> files.addAll(doApply(ftpClient, type, path)));

        operationResult.setPayload(files);
        operationResult.setSuccess(true);
        return operationResult;
    }

    private void makeLocalDirectories(String path) {
        asList(path.split(SLASH_SYMBOL)).forEach(dirName -> {
            final var directory = new File(localFilePath.concat(dirName));
            if (!directory.exists() && directory.mkdir()) {
                log.debug(format("Local directory %s created", directory.getAbsolutePath()));
            }
        });
    }

    private List<StoredFileData> doApply(FTPClient ftpClient, String type, String currentRelativePath) {
        List<StoredFileData> result = new ArrayList<>();
        if (!currentRelativePath.startsWith(SLASH_SYMBOL)) {
            currentRelativePath = SLASH_SYMBOL.concat(currentRelativePath);
        }

        try {
            final FTPFile[] ftpFiles = ftpClient.listFiles(ftpClient.printWorkingDirectory().concat(currentRelativePath));
            for (FTPFile ftpFile : ftpFiles) {
                if (!ftpFile.isDirectory() && ftpFile.getName().endsWith(type)) {
                    makeLocalDirectories(currentRelativePath);
                    ftpClient.setFileType(BINARY_FILE_TYPE);
                    String filePath = currentRelativePath.endsWith(ftpFile.getName())
                            ? currentRelativePath
                            : currentRelativePath.concat(ftpFile.getName());

                    var remoteFilePath = "%s%s".formatted(ftpClient.printWorkingDirectory(), filePath);
                    var downloadFile = new File(localFilePath + ftpFile.getName());
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
                    if (ftpClient.retrieveFile(remoteFilePath, outputStream)) {
                        result.add(new StoredFileData(downloadFile, filePath));
                    }
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            log.error("Failed to process file: %s".formatted(e.toString()));
        }
        return result;
    }
}

