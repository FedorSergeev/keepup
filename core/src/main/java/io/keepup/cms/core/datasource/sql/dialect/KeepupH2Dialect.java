package io.keepup.cms.core.datasource.sql.dialect;

import org.hibernate.dialect.H2Dialect;

/**
 *
 * @author Fedor Sergeev
 */
public class KeepupH2Dialect extends H2Dialect {

    public KeepupH2Dialect() {
        super();
        registerColumnType(-3, "varbinary");
        getDefaultProperties().setProperty("mapBlobsToBinaryType", String.valueOf(true));
    }
}
