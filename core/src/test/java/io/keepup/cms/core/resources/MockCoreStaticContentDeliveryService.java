package io.keepup.cms.core.resources;

import io.keepup.cms.core.commons.ApplicationConfig;
import io.keepup.cms.core.datasource.resources.*;

import java.io.File;

/**
 * Mock content delivery service for test purposes
 */
public class MockCoreStaticContentDeliveryService extends StaticContentDeliveryService {

    public MockCoreStaticContentDeliveryService(StorageAccessor<String> storageAccessor, ApplicationConfig applicationConfig) {
        super(storageAccessor, applicationConfig);
    }

    @Override
    public TransferOperationResult<String> store(File file, String relativePath) {
        return new TransferOperationResult<String>().ok();
    }

    @Override
    public GetFileFromStoreResult getFile(String filename, String path) {
        GetFileFromStoreResult result = new GetFileFromStoreResult();
        result.setFile(new StoredFileData(new File(filename), path));
        result.setSuccess(true);
        return result;
    }
}
