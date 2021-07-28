package io.keepup.cms.core.datasource.sql.dialect;


import org.hibernate.dialect.PostgreSQL10Dialect;

/**
 * KeepUP PostgreSQL dialect enhancements
 *
 * @author Fedor Sergeev f.sergeev@trans-it.pro
 */
public class KeepupPostgreSQLDialect extends PostgreSQL10Dialect {

    public KeepupPostgreSQLDialect() {
        super();

        registerColumnType(2004, "bytea");
        getDefaultProperties().setProperty("mapBlobsToBinaryType", String.valueOf(true));
    }
}
