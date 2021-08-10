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
 */
public class EntityUtils {

    private EntityUtils() {}

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

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
