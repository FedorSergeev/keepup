package io.keepup.cms.core.datasource.sql.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;

import static io.keepup.cms.core.datasource.sql.EntityUtils.convertToLocalDateViaInstant;

/**
 * User attribute entity. Can be used to store different data witch can be found and modified separately.
 * For some common objects {@link io.keepup.cms.core.persistence.User#setAdditionalInfo(String)} method can be used
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
@Entity
@Table(name = "user_attributes", indexes = {
        @Index(name = "IDX_USER_ATTRIBUTE_ID", columnList = "id"),
        @Index(name = "IDX__ATTRIBUTE_OWNER_USER_ID", columnList = "userid")})
public class UserAttributeEntity extends AbstractEntityAttribute {

    @Transient
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Primary key, identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_attribute_seq_generator")
    @SequenceGenerator(name = "user_attribute_seq_generator", sequenceName = "user_attribute_seq", allocationSize = 1)
    private Long id;

    /**
     * Reference to user's identifier
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    public UserAttributeEntity(Long userId, String key, Object value) {
        this.userId = userId;
        setAttributeKey(key);
        setCreationTime(convertToLocalDateViaInstant(new Date()));
        serializeValue(key, value);
    }

    public UserAttributeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public final void serializeValue(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("User attribute key cannot be null");
        }
        setAttributeKey(key);
        if (getCreationTime() == null) {
            setCreationTime(convertToLocalDateViaInstant(new Date()));
        }
        setModificationTime(convertToLocalDateViaInstant(new Date()));
        if (value == null) {
            log.warn("[USER#%d] Attribute '%s' is null".formatted(userId, key));
        } else {
            try {
                setAttributeValue(new ObjectMapper().writeValueAsBytes(value));
                setJavaClass(value.getClass().toString().substring(6));
            } catch (IOException ex) {
                log.error("Unable to convert attribute value o byte array: %s"
                        .formatted(ex.getMessage()));
                setDefaultValue();
            }
        }
    }
}
