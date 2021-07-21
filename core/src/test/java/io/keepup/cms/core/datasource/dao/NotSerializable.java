package io.keepup.cms.core.datasource.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NotSerializable {
    private final Log log = LogFactory.getLog(getClass());
    private int integer;

    public NotSerializable() {
        log.info("Not serializable object instantiated");
    }

    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }
}
