package io.keepup.cms.core.datasource.sql.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serial;
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
@org.springframework.data.relational.core.mapping.Table
@Table(name = "user_attribute", indexes = {
        @Index(name = "IDX_USER_ATTRIBUTE_ID", columnList = "id"),
        @Index(name = "IDX__ATTRIBUTE_OWNER_USER_ID", columnList = "userid")})
public class UserAttributeEntity extends AbstractEntityAttribute {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(UserAttributeEntity.class);

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

    /**
     * Constructor with parameters.
     *
     * @param userId user's primary identifier
     * @param key    attribute key
     * @param value  attribute value
     */
    public UserAttributeEntity(Long userId, String key, Object value) {
        this.userId = userId;
        setAttributeKey(key);
        setCreationTime(convertToLocalDateViaInstant(new Date()));
        serializeValue(key, value);
    }

    /**
     * Default constructor.
     */
    public UserAttributeEntity() {
    }

    /**
     * Get attribute ID.
     *
     * @return attribute ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set attribute ID.
     *
     * @param id attribute ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get user's ID.
     *
     * @return user's ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Set user's ID.
     *
     * @param userId user's ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Serialize object as byte array for saving it as user's attribute value.
     *
     * @param key   user's attribute name
     * @param value user's attribute value
     */
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
            getLog().warn("[USER#%d] Attribute '%s' is null".formatted(userId, key));
        } else {
            try {
                setAttributeValue(mapper.writeValueAsBytes(value));
                setJavaClass(value.getClass().toString().substring(6));
            } catch (IOException ex) {
                getLog().error("Unable to convert attribute value o byte array: %s"
                        .formatted(ex.getMessage()));
                setDefaultValue();
            }
        }
    }

    /**
     * Get logger instance.
     *
     * @return logger instance
     */
    @Override
    protected Log getLog() {
        return LOG;
    }
}
