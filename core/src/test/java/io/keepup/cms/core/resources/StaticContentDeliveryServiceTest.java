package io.keepup.cms.core.resources;

import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.config.KeepupConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.resources.*;
import io.keepup.cms.core.plugins.TestAbstractKeepupDeployBeanImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.core.server.AbstractFtpServer;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ContextConfiguration(classes = {
        KeepupApplication.class,
        KeepupCacheConfiguration.class,
        CacheAdapter.class,
        WebFluxConfig.class,
        KeepupConfiguration.class,
        ApplicationConfig.class,
        TestAbstractKeepupDeployBeanImpl.class,
        StaticContentDeliveryService.class,
        FtpFileProcessor.class
})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
class StaticContentDeliveryServiceTest {

    private FakeFtpServer fakeFtpServer;

    @Autowired
    private StaticContentDeliveryService contentDeliveryService;
    @Autowired
    private ApplicationConfig applicationConfig;

    private String staticPath;

    @BeforeEach
    void setUp() throws IOException {
        staticPath = applicationConfig.getStaticPath();
        if (!new File(staticPath).exists()) Files.createDirectory(Paths.get(staticPath));
    }

    @AfterEach
    public void tearDown() {
        ofNullable(fakeFtpServer).ifPresent(AbstractFtpServer::stop);
    }

    @Test
    void getStorageType() throws NoSuchFieldException, IllegalAccessException {
        setStorageType(1);
        setUpFakeServer("user");
        assertEquals(StorageType.FTP, contentDeliveryService.getStorageType());
    }

    @Test
    void store() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        setStorageType(1);
        setUpFakeServer("user");

        final File fileToUpload = new File(getClass().getClassLoader().getResource("application.yaml").toURI());
        final TransferOperationResult transferOperationResult = contentDeliveryService.store(fileToUpload, "");

        assertTrue(transferOperationResult.isSuccess());
        assertEquals(0, transferOperationResult.getCode());
        assertEquals("ok", transferOperationResult.getMessage());
        assertNotNull(fakeFtpServer.getFileSystem().getEntry("/data/application.yaml"));
    }

    @Test
    void storeWithoutFile() throws NoSuchFieldException, IllegalAccessException {
        setStorageType(1);
        setUpFakeServer("user");

        final File fileToUpload = new File("unknown_file");
        final TransferOperationResult transferOperationResult = contentDeliveryService.store(fileToUpload, "");

        assertFalse(transferOperationResult.isSuccess());
        assertEquals(1, transferOperationResult.getCode());
        assertTrue(transferOperationResult.getMessage().contains("java.io.FileNotFoundException: unknown_file"));
    }

    @Test
    void storeInFilesystem() throws NoSuchFieldException, IllegalAccessException, URISyntaxException {
        setStorageType(0);
        final File fileToUpload = new File(getClass().getClassLoader().getResource("application.yaml").toURI());
        final TransferOperationResult transferOperationResult = contentDeliveryService.store(fileToUpload, "");

        assertTrue(transferOperationResult.isSuccess());
        assertEquals(0, transferOperationResult.getCode());
        assertEquals("ok", transferOperationResult.getMessage());
    }

    @Test
    void storeWithoutCredentials() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        setStorageType(1);
        setUpFakeServer("user123");

        final File fileToUpload = new File(getClass().getClassLoader().getResource("application.yaml").toURI());
        final TransferOperationResult transferOperationResult = contentDeliveryService.store(fileToUpload, "");
        assertFalse(transferOperationResult.isSuccess());
        assertEquals(1, transferOperationResult.getCode());
        assertEquals("Failed to upload file", transferOperationResult.getMessage());
    }

    @Test
    void storeWithoutServer() throws URISyntaxException, NoSuchFieldException, IllegalAccessException {
        setStorageType(1);
        final File fileToUpload = new File(getClass().getClassLoader().getResource("application.yaml").toURI());
        final TransferOperationResult transferOperationResult = contentDeliveryService.store(fileToUpload, "");
        assertFalse(transferOperationResult.isSuccess());
        assertEquals(1, transferOperationResult.getCode());
        assertTrue(transferOperationResult.getMessage().contains("Connection refused"));
    }

    @Test
    void getFile() throws NoSuchFieldException, IllegalAccessException {
        setStorageType(1);
        setUpFakeServer("user");
        fakeFtpServer.getFileSystem().add(new FileEntry("/data/some_file"));
        final GetFileFromStoreResult someFile = contentDeliveryService.getFile("some_file", "");
        assertNotNull(someFile);
        assertTrue(someFile.isSuccess());
    }

    @Test
    void getFileInFilesystem() throws NoSuchFieldException, IllegalAccessException, IOException {
        setStorageType(0);
        var file = new File("%s/%s".formatted(staticPath, "some_file"));
        file.createNewFile();
        final var someFile = contentDeliveryService.getFile("some_file", "");
        assertNotNull(someFile);
        assertTrue(someFile.isSuccess());
    }

    @Test
    void getFileInFilesystemWithNullPath() throws NoSuchFieldException, IllegalAccessException, IOException {
        setStorageType(0);
        new File(staticPath.concat("some_file")).createNewFile();
        final var someFile = contentDeliveryService.getFile("some_file", null);
        assertNotNull(someFile);
        assertFalse(someFile.isSuccess());
        assertEquals("Path parameter is not specified", someFile.getMessage());
    }

    @Test
    void getFileInFilesystemWithIncorrectPath() throws NoSuchFieldException, IllegalAccessException, IOException {
        setStorageType(0);
        new File(staticPath.concat("some_file")).createNewFile();
        final var someFile = contentDeliveryService.getFile("some_file", "incorrect");
        assertNotNull(someFile);
        assertFalse(someFile.isSuccess());
        assertEquals("Wrong path to file: incorrect", someFile.getMessage());
    }

    @Test
    void getByType() throws NoSuchFieldException, IllegalAccessException {
        setStorageType(1);
        setUpFakeServer("user");
        fakeFtpServer.getFileSystem().add(new FileEntry("/data/some_file.ext"));
        final var someFile = contentDeliveryService.getByType("ext", "/");
        assertNotNull(someFile);
        assertTrue(someFile.isSuccess());
    }

    @Test
    void getByTypeInFilesystem() throws NoSuchFieldException, IllegalAccessException, IOException {
        setStorageType(0);
        final String filename = "some_text_file.txt";
        new File("%s/%s".formatted(staticPath, filename)).createNewFile();
        final GetTreeFromStoreResult someFiles = contentDeliveryService.getByType("txt", "/");
        assertNotNull(someFiles);
        assertTrue(someFiles.isSuccess());
        assertFalse(someFiles.getFiles().isEmpty());
        assertTrue(someFiles.getFiles().stream().anyMatch(file -> filename.equals(file.getFile().getName())));
    }

    private void setStorageType(int storageType) throws NoSuchFieldException, IllegalAccessException {
        final Field storageTypeField = StaticContentDeliveryService.class.getDeclaredField("storageType");
        storageTypeField.setAccessible(true);
        storageTypeField.set(contentDeliveryService, storageType);
        storageTypeField.setAccessible(false);
    }

    private void setUpFakeServer(String username) {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount(username, "password", "/data"));
        final org.mockftpserver.fake.filesystem.FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/file.txt", "test content"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(4000);

        fakeFtpServer.start();
    }
}