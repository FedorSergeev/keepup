package io.keepup.cms.core.datasource.resources;

/**
 * Number of strategies for storing frontend static resources
 */
public enum StorageType {
    FILESYSTEM(0), FTP(1);

    private int value;

    public int getValue() {
        return value;
    }

    public static StorageType valueOf(int type) {
        for (StorageType storageType : StorageType.values()) {
            if (storageType.getValue() == type) {
                return storageType;
            }
        }
        return null;
    }

    StorageType(int type) {
        this.value = type;
    }
}
