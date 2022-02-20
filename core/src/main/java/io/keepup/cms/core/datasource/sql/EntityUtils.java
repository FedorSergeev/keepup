package io.keepup.cms.core.datasource.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Helper with static methods for some utility purposes
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public class EntityUtils {

    private EntityUtils() {}

    /**
     * Converts object to byte array for persisting attribute values in the same storage. In case of
     * IOException during serialization process no Throwable will be thrown and the an empty byte array will be
     * returned as operation result. Maybe catching exception and wrapping it as a specific system exception
     * is a better approach then checking the length of method result, but yet the process is design for user to
     * receive response for his request anyway.
     *
     * @param value object to be serialized to byte array
     * @return      object serialized to array of bytes
     */
    public static byte[] toByteArray(Serializable value) {
        if (value == null) {
            return new byte[0];
        }
        try {
            return new ObjectMapper().writeValueAsBytes(value);
        } catch (IOException ex) {
            LogFactory.getLog(EntityUtils.class).error("Failed to encode value %s to byte array: %s".formatted(value, ex.toString()));
            return new byte[0];
        }
    }

    /**
     * Date to {@link LocalDate} converter, useful for storing dates in databases.
     *
     * @param dateToConvert object as {@link Date}
     * @return              object as {@link LocalDate}
     */
    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
