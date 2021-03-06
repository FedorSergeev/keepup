package io.keepup.cms.core.datasource.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static java.lang.String.format;

/**
 * Separate parametrized class for FTP operations invocation.
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public class FtpOperationExecutor<T> {
    private static final String ERROR_DISCONNECT = "Disconnecting from FTP server as reply code is not positive";

    private final Log log = LogFactory.getLog(getClass());

    private final String username;
    private final String password;
    private final String server;
    private final int port;

    /**
     * Executor object for FTP transfer operations.
     *
     * @param username username for logging in the remote FTP server
     * @param password password for logging in the remote FTP server
     * @param server   remote FTP server host
     * @param port     remote FTP server port
     */
    public FtpOperationExecutor(String username, String password, String server, int port) {
        log.info("Setting up FTP executor with server %s and username = %s".formatted(server, username));
        this.username = username;
        this.password = password;
        this.server = server;
        this.port = port;
    }

    /**
     * Perform an FTP operation.
     *
     * @param file          file to work with
     * @param relativePaths logical path to file
     * @param operation     operation type
     * @return              result of transfer operation with description
     */
    public TransferOperationResult<T> doFtpOperation(File file, List<String> relativePaths, FtpOperation<T> operation) {
        TransferOperationResult<T> transferOperationResult;
        var ftpClient = new FTPClient();
        ftpClient.setRemoteVerificationEnabled(false);
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(new LogOutputStream(log))));

        try {
            if (!ftpClient.isConnected()) {
                ftpClient.connect(server, port);
            }
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                log.error(ERROR_DISCONNECT);
                return new TransferOperationResult<T>().error(ERROR_DISCONNECT);
            } else {
                ftpClient.login(username, password);
                makeDirectories(relativePaths, ftpClient);
                transferOperationResult = operation.apply(ftpClient, file, relativePaths);
                ftpClient.logout();
            }
        } catch (IOException e) {
            log.error(format("Failed o connect to FTP server: %s", e.toString()));
            transferOperationResult = new TransferOperationResult<T>().error(e.getMessage());
        }
        return transferOperationResult;
    }

    private void makeDirectories(List<String> relativePaths, FTPClient ftpClient) throws IOException {
        for(String path : relativePaths) {
            FtpUtils.makeDirectories(ftpClient, path);
        }
    }
}
