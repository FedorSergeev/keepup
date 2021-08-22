package io.keepup.cms.core.datasource.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

/**
 * FTP operation or getting list of files from remote directory recursively
 */
public class GetFilesOperation implements FtpOperation<List<StoredFileData>> {
    public static final String SLASH_SYMBOL = "/";

    private final Log log = LogFactory.getLog(getClass());
    private final String relativePath;
    private final String localFilePath;

    public GetFilesOperation(String relativePath, String dumpFilePath) {
        localFilePath = "%s/ftp/".formatted(dumpFilePath);
        final var localFileDirectory = new File(localFilePath);
        this.relativePath = relativePath;
        if (!localFileDirectory.exists() && localFileDirectory.mkdir()) {
            log.debug("Local directory for files temporary storage created with path " + localFilePath);
        }
    }

    @Override
    public TransferOperationResult<List<StoredFileData>> apply(FTPClient ftpClient, Object... parameters) {
        String currentRelativePath = parameters == null || parameters.length == 0 || !(parameters[parameters.length - 1] instanceof String)
                ? relativePath
                : (String)parameters[parameters.length - 1];

        if (currentRelativePath == null) {
            return new TransferOperationResult<List<StoredFileData>>().error("Empty relative path specified in request");
        }

        final TransferOperationResult<List<StoredFileData>> operationResult = new TransferOperationResult<>();
        operationResult.setPayload(new ArrayList<>());
        List<StoredFileData> files = new ArrayList<>();
        if (!currentRelativePath.startsWith(SLASH_SYMBOL)) {
            currentRelativePath = SLASH_SYMBOL.concat(currentRelativePath);
        }

        try {
            final FTPFile[] ftpFiles = ftpClient.listFiles(ftpClient.printWorkingDirectory().concat(currentRelativePath));
            for (FTPFile ftpFile : ftpFiles) {
                processFtpFile(ftpClient, currentRelativePath, operationResult, files, ftpFile);
            }
        } catch (
                IOException e) {
            return new TransferOperationResult<List<StoredFileData>>().error(e.toString());
        }
        operationResult.setPayload(files);
        operationResult.setSuccess(true);
        return operationResult;
    }

    private void processFtpFile(FTPClient ftpClient, String currentRelativePath, TransferOperationResult<List<StoredFileData>> operationResult, List<StoredFileData> files, FTPFile ftpFile) throws IOException {
        if (ftpFile.isDirectory()) {
            final String directoryName = currentRelativePath.concat(ftpFile.getName()).concat(SLASH_SYMBOL);
            makeLocalDirectories(directoryName);
            stream(ofNullable(ftpClient.listFiles(ftpClient.printWorkingDirectory().concat(directoryName)))
                    .orElse(new FTPFile[0]))
                    .map(file -> apply(ftpClient, directoryName.concat(file.getName())))
                    .forEach(filesFromDirectory -> {
                        if (!filesFromDirectory.isSuccess()) {
                            operationResult.setSuccess(false);
                            operationResult.setMessage(operationResult.getMessage().concat(filesFromDirectory.getMessage()));
                        }
                        files.addAll(filesFromDirectory.getPayload());
                    });
        } else {

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String filePath = currentRelativePath.endsWith(ftpFile.getName())
                    ? currentRelativePath
                    : currentRelativePath.concat(ftpFile.getName());

            var remoteFilePath = ftpClient.printWorkingDirectory() + filePath;
            var downloadFile = new File(localFilePath + ftpFile.getName());
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            if (ftpClient.retrieveFile(remoteFilePath, outputStream)) {
                files.add(new StoredFileData(downloadFile, relativePath));
                operationResult.setSuccess(true);
            } else {
                operationResult.setMessage(operationResult.getMessage().concat("Failed to download file " + remoteFilePath + ";\n"));
            }
            outputStream.close();
        }
    }

    private void makeLocalDirectories(String path) {
        asList(path.split(SLASH_SYMBOL)).forEach(dirName -> {
            final var directory = new File(localFilePath.concat(dirName));
            if (!directory.exists() && directory.mkdir()) {
                log.debug(format("Local directory %s created", directory.getAbsolutePath()));
            }
        });
    }
}
