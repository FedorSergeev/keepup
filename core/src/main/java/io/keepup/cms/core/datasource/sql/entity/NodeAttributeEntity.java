package io.keepup.cms.core.datasource.sql.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import static io.keepup.cms.core.datasource.sql.EntityUtils.convertToLocalDateViaInstant;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Persistent node attribute
 *
 * @author Fedor Sergeev
 */
@Entity
@org.springframework.data.relational.core.mapping.Table
@Table(name = "node_attribute", indexes = {
        @Index(name = "IDX_ATTRIBUTE_ID", columnList = "id"),
        @Index(name = "IDX_CONTENT_ID", columnList = "content_id")})
public class NodeAttributeEntity extends AbstractEntityAttribute {

    @Serial
    private static final long serialVersionUID = 507224019294570770L;
    private static final Log LOG = LogFactory.getLog(NodeAttributeEntity.class);
    private static final String NULL = "null";
    /**
     * Primary identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_attribute_seq_generator")
    @SequenceGenerator(name = "content_attribute_seq_generator", sequenceName = "content_attribute_seq", allocationSize = 1)
    private Long id;
    /**
     * Identifier of {@link NodeEntity} which current attribute entity belongs to
     */
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    /**
     * Default constructor, no additional logic is implemented.
     */
    public NodeAttributeEntity() {
    }

    /**
     * Crete new object with the specified parameters.
     *
     * @param contentId identifier of {@link NodeEntity} which cirrent object belongs to
     * @param key       attribute key
     * @param value     attribute value
     */
    public NodeAttributeEntity(final Long contentId, final String key, final Serializable value) {

        this.contentId = contentId;
        setAttributeKey(key);
        setCreationTime(convertToLocalDateViaInstant(new Date()));
        setModificationTime(convertToLocalDateViaInstant(new Date()));
        if (value == null) {
            getLog().warn("[NODE#%d] Attribute '%s' is null".formatted(contentId, key));
        } else {
            try {
                setAttributeValue(mapper.writeValueAsBytes(value));
                setJavaClass(value.getClass().toString().substring(6));
            } catch (IOException ex) {
                getLog().error("Unable to convert attribute value o byte array: %s".formatted(ex.getMessage()));
                setDefaultValue();
            }
        }
    }

    /**
     * Crete new object with the specified parameters.
     *
     * @param id        entity primary identifier
     * @param contentId identifier of {@link NodeEntity} which cirrent object belongs to
     * @param key       attribute key
     * @param value     attribute value
     */
    public NodeAttributeEntity(final Long id, final Long contentId, final String key, final Serializable value) {
        this(contentId, key, value);
        this.id = id;
    }

    /**
     * Get entity ID.
     *
     * @return entity ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set entity ID.
     *
     * @param id entity ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get ID of {@link NodeEntity} which current object belongs to.
     *
     * @return ID of {@link NodeEntity} which current object belongs to
     */
    public Long getContentId() {
        return contentId;
    }

    /**
     * Set ID of {@link NodeEntity} which current object belongs to.
     *
     * @param contentId ID of {@link NodeEntity} which current object belongs to
     */
    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    @Override
    public final String toString() {
        var stringAttributeValue = EMPTY;

        if (getAttributeValue() == null || getJavaClass() == null) {
            stringAttributeValue = "NULL";
        } else {
            try {
                stringAttributeValue = new ObjectMapper().readValue(getAttributeValue(), Class.forName(getJavaClass())).toString();
            } catch (IOException | ClassNotFoundException ex) {
                LogFactory.getLog(this.getClass()).error(format("Could not parse attribute value: %s", ex.getMessage()));
            }
        }
        String strId;
        if (id == null) {
            strId = "[NOT SET]";
        } else {
            strId = Long.toString(id);
        }
        return "id= ".concat(strId)
                .concat(" contentId=").concat(ofNullable(contentId).map(NodeAttributeEntity::apply).orElse(EMPTY))
                .concat(" attributeKey=").concat(ofNullable(getAttributeKey()).orElse(NULL))
                .concat(" attributeValue=").concat(ofNullable(stringAttributeValue).orElse(NULL))
                .concat(" creationTime=").concat(ofNullable(getCreationTime()).map(LocalDate::toString).orElse(NULL))
                .concat(" modificationTime=").concat(ofNullable(getModificationTime()).map(LocalDate::toString).orElse(NULL));
    }

    private static String apply(Long cId) {
        return Long.toString(cId);
    }

    @Override
    protected Log getLog() {
        return LOG;
    }
}
