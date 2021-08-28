package io.keepup.cms.core.datasource.resources;

import org.apache.commons.net.ftp.FTPClient;

/**
 * File transfer operation contract
 */
@FunctionalInterface
public interface FtpOperation<T> {
    /**
     * Do some operation via FTP
     *
     * @param ftpClient  FTP client object
     * @param parameters operation parameters, eg file and it's path
     * @return           operation result
     */
    TransferOperationResult<T> apply(FTPClient ftpClient, Object... parameters);
}
