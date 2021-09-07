package io.keepup.cms.core.resources;

import io.keepup.cms.core.datasource.resources.GetFilesOperation;
import io.keepup.cms.core.datasource.resources.StaticContentDeliveryService;
import io.keepup.cms.core.datasource.resources.StoredFileData;
import io.keepup.cms.core.datasource.resources.TransferOperationResult;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GetFilesOperationTest {
    private final String dump = Paths.get(".").toAbsolutePath().normalize().toString().concat("/dump");
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String SERVER = "localhost";
    private static final int PORT = 4000;


    private FakeFtpServer fakeFtpServer;
    private FTPClient ftpClient;
    private GetFilesOperation operation;

    @BeforeEach
    public void setUp() throws Exception {
        setUpFakeServer(USER);
        ftpClient = new FTPClient();
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
    }

    @AfterEach
    public void tearDown() {
        fakeFtpServer.stop();
    }

    @Test
    public void applyOk() throws IOException {
        TransferOperationResult operationResult = null;
        operation = new GetFilesOperation("", dump);
        ftpClient.connect(SERVER, PORT);

        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            ftpClient.disconnect();
        } else {
            ftpClient.login(USER, PASSWORD);
            operationResult = operation.apply(ftpClient, null, "test.txt");
            ftpClient.logout();
        }
        ftpClient.disconnect();

        assertNotNull(operationResult);
        assertEquals(0, operationResult.getCode());
        assertEquals(ArrayList.class, operationResult.getPayload().getClass());
        assertFalse(((List<StoredFileData>)operationResult.getPayload()).isEmpty());
        assertNotNull(((List<StoredFileData>)operationResult.getPayload()).get(0).getFile());
        assertEquals("test.txt", ((List<StoredFileData>)operationResult.getPayload()).get(0).getFile().getName());
        assertTrue(operationResult.isSuccess());
    }

    @Test
    public void applyRecursively() throws IOException {
        TransferOperationResult operationResult = null;
        operation = new GetFilesOperation("", dump);
        ftpClient.connect(SERVER, PORT);

        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            ftpClient.disconnect();
        } else {
            ftpClient.login(USER, PASSWORD);
            operationResult = operation.apply(ftpClient, null, "/");
            ftpClient.logout();
        }
        ftpClient.disconnect();

        assertNotNull(operationResult);
        assertEquals(0, operationResult.getCode());
        assertEquals(ArrayList.class, operationResult.getPayload().getClass());
        assertEquals(2, ((List<StoredFileData>)operationResult.getPayload()).size());
        assertFalse(((List<StoredFileData>)operationResult.getPayload()).isEmpty());
        assertNotNull(((List<StoredFileData>)operationResult.getPayload()).get(0).getFile());
        assertNotNull(((List<StoredFileData>)operationResult.getPayload()).get(0).getPath());
        assertTrue(operationResult.isSuccess());
    }

    @Test
    public void applyEmpty() throws IOException {
        TransferOperationResult operationResult = null;
        operation = new GetFilesOperation("/", dump);
        ftpClient.connect(SERVER, PORT);

        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            ftpClient.disconnect();
        } else {
            ftpClient.login(USER, PASSWORD);
            operationResult = operation.apply(ftpClient, null, "incorrect_path/test.txt");
            ftpClient.logout();
        }
        ftpClient.disconnect();

        assertNotNull(operationResult);
        assertEquals(0, operationResult.getCode());
        assertEquals(ArrayList.class, operationResult.getPayload().getClass());
        assertTrue(((List<StoredFileData>)operationResult.getPayload()).isEmpty());
        assertTrue(operationResult.isSuccess());
    }

    private void setUpFakeServer(String username) throws NoSuchFieldException {

        final Field storageTypeField = StaticContentDeliveryService.class.getDeclaredField("storageType");
        storageTypeField.setAccessible(true);
        storageTypeField.setAccessible(false);

        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount(username, PASSWORD, "/data"));
        final FileSystem fileSystem = new UnixFakeFileSystem();
        fakeFtpServer.setFileSystem(fileSystem);
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new DirectoryEntry("/data/inner_directory"));
        fileSystem.add(new FileEntry("/data/inner_directory/test_1.txt"));
        fileSystem.add(new FileEntry("/data/test.txt"));

        fakeFtpServer.setServerControlPort(4000);
        fakeFtpServer.start();
    }
}