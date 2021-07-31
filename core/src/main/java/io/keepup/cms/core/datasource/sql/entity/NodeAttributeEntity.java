package io.keepup.cms.core.datasource.sql.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Persistent node attribute
 *
 * @author Fedor Sergeev f.sergeev@trans-it.pro
 */
@Entity
@Table(name = "node_attributes", indexes = {
        @Index(name = "IDX_ATTRIBUTE_ID", columnList = "id"),
        @Index(name = "IDX_CONTENT_ID", columnList = "content_id")})
public class NodeAttributeEntity extends AbstractEntityAttribute {

    @Serial
    private static final long serialVersionUID = 507224019294570770L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_attribute_seq_generator")
    @SequenceGenerator(name = "content_attribute_seq_generator", sequenceName = "content_attribute_seq", allocationSize = 1)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    public NodeAttributeEntity() {
    }

    public NodeAttributeEntity(Long contentId, String key, Serializable value) {
        final var log = LogFactory.getLog(getClass());
        this.contentId = contentId;
        setAttributeKey(key);
        setCreationTime(convertToLocalDateViaInstant(new Date()));
        setModificationTime(convertToLocalDateViaInstant(new Date()));
        if (value == null) {
            log.warn(format("[NODE#%d] Attribute '%s' is null, setting value tu empty byte array", contentId, key));
            setDefaultValue();
        } else {
            try {
                setAttributeValue(new ObjectMapper().writeValueAsBytes(value));
                setJavaClass(value.getClass().toString().substring(6));
            } catch (IOException ex) {
                log.error(format("Unable to convert attribute value o byte array: %s", ex.getMessage()));
                setDefaultValue();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    @Override
    public final String toString() {
        var stringAttributeValue = StringUtils.EMPTY;
        if (getAttributeValue() == null) {
            setDefaultValue();
        }
        try {
            stringAttributeValue = new ObjectMapper().readValue(getAttributeValue(), Class.forName(getJavaClass())).toString();
        } catch (IOException | ClassNotFoundException ex) {
            LogFactory.getLog(this.getClass()).error(format("Could not parse attribute value: %s", ex.getMessage()));
        }
        String strId;
        if (id == null) {
            strId = "[NOT SET]";
        } else {
            strId = Long.toString(id);
        }
        return "id= ".concat(strId)
                .concat(" contentId=").concat(Optional.ofNullable(contentId).map(NodeAttributeEntity::apply).orElse(StringUtil.EMPTY_STRING))
                .concat(" attributeKey=").concat(getAttributeKey())
                .concat(" attributeValue=").concat(stringAttributeValue)
                .concat(" creationTime=").concat(getCreationTime().toString())
                .concat(" modificationTime=").concat(Optional.ofNullable(getModificationTime()).map(LocalDate::toString).orElse("null"));
    }

    public NodeAttributeEntity(Long id, Long contentId, String key, Serializable value) {
        this(contentId, key, value);
        this.id = id;
    }

    private static String apply(Long cId) {
        return Long.toString(cId);
    }


    private static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

}
