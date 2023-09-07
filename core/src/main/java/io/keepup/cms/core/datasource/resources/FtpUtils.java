package io.keepup.cms.core.datasource.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import static java.lang.String.format;

/**
 * Utility class for FTP operations.
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
public final class FtpUtils {
    private static final Log LOG = LogFactory.getLog(FtpUtils.class);
    /**
     * Slash symbol
     */
    public static final String SLASH = "/";

    /**
     * Create directory recursively at the remote FTP server.
     *
     * @param ftpClient    FTP client
     * @param dirPath      path to directory starting from client root
     * @throws IOException if an I/O error occurs while either sending a command to the server or receiving a reply from
     *                     the server.
     */
    public static void makeDirectories(FTPClient ftpClient, String dirPath) throws IOException {
        if (dirPath.isEmpty()) {
            return;
        }
        String[] pathElements = dirPath.split(SLASH);

        for (String singleDir : pathElements) {
            boolean existed = ftpClient.changeWorkingDirectory(singleDir);
            if (!existed) {
                boolean created = ftpClient.makeDirectory(singleDir);
                if (created) {
                    LOG.info(format("CREATED directory: %s", singleDir));
                    ftpClient.changeWorkingDirectory(singleDir);
                } else {
                    LOG.error(format("FAILED to create directory: %s", singleDir));
                    return;
                }
            }
        }

    }

    private FtpUtils() {}
}
