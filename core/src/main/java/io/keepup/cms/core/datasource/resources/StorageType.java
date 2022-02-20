package io.keepup.cms.core.datasource.resources;

/**
 * Number of strategies for storing frontend static resources
 */
public enum StorageType {
    /**
     * Files are stored at the filesystem where the application runs.
     */
    FILESYSTEM(0),
    /**
     * Files are stored at remote host using FTP protocol.
     */
    FTP(1);

    private int value;

    /**
     * Get storage type value.
     *
     * @return numeric value according to current type
     */
    public int getValue() {
        return value;
    }

    /**
     * Create {@link StorageType} object from numeric value.
     *
     * @param type numeric type
     * @return     StorageType corresponding to the given number
     */
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
