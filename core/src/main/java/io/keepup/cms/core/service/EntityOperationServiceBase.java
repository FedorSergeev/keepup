package io.keepup.cms.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.datasource.dao.DataSourceFacade;
import io.keepup.cms.core.persistence.Content;
import io.keepup.cms.core.persistence.Node;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static reactor.core.publisher.Mono.empty;

/**
 * Provides CRUD methods for work with different turn entities.
 * <p>
 * Abstract routine for CRUD service that provide operations with different turn entities, eg route operations.
 *
 * @author Fedor Sergeev
 * @version 2.0
 */
public abstract class EntityOperationServiceBase<T> implements EntityService<T> {

    private static final String TO_STRING_METHOD = "toString";

    private final Class<T> typeClass;

    private ObjectMapper mapper;

    protected final Log log = LogFactory.getLog(getClass());
    protected List<Long> entityParentIds;
    protected DataSourceFacade dataSourceFacade;

    protected EntityOperationServiceBase() {
        entityParentIds = asList(0L);
        this.typeClass = getGenericParameterClass(getClass());
    }

    @Autowired
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public void setDataSourceFacade(DataSourceFacade dataSourceFacade) {
        this.dataSourceFacade = dataSourceFacade;
    }

    /**
     * Basic method for mapping data source records to POJOs
     *
     * @param id object identifier
     * @return is not supported in abstract implementation as convert method should be implemented
     * in the concrete class.
     */
    @Override
    public Mono<T> get(Long id) {
        return dataSourceFacade.getContentByIdAndType(id, typeClass.getTypeName())
                .map(this::convert);
    }

    /**
     * Fetches all {@link Content} records with the specified parent ids and attempts to
     * convert them to the generic entity. There is one point that needs to be discussed:
     * if there is a number of different object types with the same parent id they all will
     * be converted to type specified by service and if it not possible an Exception will
     * be thrown
     * @return Publisher that produces filtered converted entities
     */
    @Override
    public Flux<T> getAll() {
        return dataSourceFacade.getContentByParentIdsAndType(entityParentIds, this.typeClass.getTypeName())
                .map(this::convert);
    }

    /**
     * Removes selected entity by id
     *
     * @param id entity identifier
     */
    public Mono<Void> delete(Long id) {
        return dataSourceFacade.deleteContent(id);
    }

    /**
     * Gets the list of current node parent identifiers till the root node
     *
     * @return list of identifiers
     */
    public List<Long> getEntityParentIds() {
        return entityParentIds;
    }

    /**
     * Sets the list of current node parent identifiers till the root node
     */
    public void setEntityParentIds(List<Long> entityParentIds) {
        this.entityParentIds = entityParentIds;
    }

    /**
     * Creates a copy of an entity and saves it to the data source. No validations are present in default
     * implementation.
     *
     * @param entityId identifier of an entity to copy
     * @return identifier of new entity
     */
    public Mono<Long> copy(Long entityId) {
        return dataSourceFacade.getContent(entityId)
                .flatMap(dataSourceFacade::createContent);
    }

    /**
     * Creates the new entity or updates the existing one
     *
     * @param entity  object to be saved
     * @param ownerId user's identifier
     * @return copy of saved instance
     */
    @Transactional
    public Mono<T> save(T entity, long ownerId) {
        if (entityParentIds == null || entityParentIds.isEmpty()) {
            log.error("Cannot save entity %s under null parent id".formatted(typeClass.getTypeName()));
            return empty();
        }
        if (entity == null) {
            log.warn("Empty entity cannot be saved for ownerId %s".formatted(ownerId));
            return empty();
        }
        if (noDefaultConstructor()) {
            log.warn("Entities of type %s have no default constructor, entity %s won't be saved".formatted(typeClass.getName(), entity.toString()));
            return empty();
        }
        var serializedEntity = new Node();
        serializedEntity.setParentId(entityParentIds.get(0));
        serializedEntity.setOwnerId(ownerId);
        serializedEntity.setDefaultPrivileges();
        serializedEntity.setEntityType(getValueClassName(entity));
        Object fieldValue = null;
        final AtomicReference<Field> idField = new AtomicReference<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            fieldValue = processField(entity, serializedEntity, fieldValue, idField, field);
        }
        return dataSourceFacade.createContent(serializedEntity)
                .map(savedId -> {
                    String idFieldName = ofNullable(idField.get())
                            .map(Field::getName)
                            .orElse(StringUtils.EMPTY);
                    Field entityIdField;
                    try {
                        entityIdField = entity.getClass().getDeclaredField(idFieldName);
                        FieldUtils.writeField(entityIdField, entity, savedId, true);

                    } catch (NoSuchFieldException e) {
                        log.error("No field with name '%s' found for class %s"
                                .formatted(idFieldName, entity.getClass()));
                    } catch (IllegalArgumentException e) {
                        log.error("Id field is null or value is not assignable: %s"
                                .formatted(e.toString()));
                    } catch (IllegalAccessException | SecurityException e) {
                        log.error("Id field cannot be accessed for object %s: %s"
                                .formatted(entity.toString(), e.toString()));
                    }
                    return entity;
                });

    }

    private Object processField(T entity, Node serializedEntity, Object fieldValue, AtomicReference<Field> idField, Field field) {
        Object newFieldValue;
        try {
            fieldValue = FieldUtils.readField(field, entity, true);
        } catch (IllegalAccessException e) {
            log.error("Could not add attribute to map: %s".formatted(e.toString()));
        }

        if (field.isAnnotationPresent(ContentMapping.class)) {

            newFieldValue = getSerializedValue(fieldValue);

            serializedEntity.addAttribute(field.getAnnotation(ContentMapping.class).value(), newFieldValue);
        } else if (field.isAnnotationPresent(ContentId.class)) {
            setIdField(entity, serializedEntity, idField, field);
        }
        return fieldValue;
    }

    private void setIdField(T entity, Node serializedEntity, AtomicReference<Field> idField, Field field) {
        var idValue = getIdValue(entity, field);
        serializedEntity.setId(idValue);
        idField.set(field);
        log.debug("Id field %s = %s set to as Content record identifier".formatted(field.getName(), idValue));
    }

    /**
     * Check if field is Serializable and wrap it with proxy if not
     *
     * @param fieldValue incoming field value
     * @return           java.io.Serializable version of field value
     */
    @Nullable
    private Object getSerializedValue(Object fieldValue) {
        Object newFieldValue;
        // region
        final Class<?> fieldType = ofNullable(fieldValue)
                .map(Object::getClass)
                .orElse(null);
        if (fieldType != null && !Serializable.class.isAssignableFrom(fieldType)) {
            var enhancer = new Enhancer();
            enhancer.setSuperclass(fieldType);
            enhancer.setInterfaces(new Class[]{Serializable.class});
            enhancer.setCallbackFilter(this::getCallbackFilter);
            enhancer.setCallbacks(getCallbacks());
            newFieldValue = enhancer.create();
            for (Field attributeField : fieldType.getDeclaredFields()) {
                setValue(newFieldValue, attributeField, getValue(fieldValue, attributeField));
            }
        } else {
            newFieldValue = fieldValue;
        }
        //endregion
        return newFieldValue;
    }

    private boolean noDefaultConstructor()  {
        try {
            return typeClass.getDeclaredConstructor() == null;
        } catch (NoSuchMethodException e) {
            log.error("Exception while fetching the default constructor for type %s: "
                    .formatted(typeClass.getTypeName(), e.toString()));
            return true;
        }
    }

    private int getCallbackFilter(Method method) {
        if (TO_STRING_METHOD.equals(method.getName()) && method.getReturnType() == String.class) {
            return 0;
        }
        return 1;
    }

    private Callback[] getCallbacks() {
        return new Callback[]{(MethodInterceptor) (obj, method, args, proxy) -> mapper.writeValueAsString(obj),
                (MethodInterceptor) (obj, method, args, proxy) -> proxy.invokeSuper(obj, args)
        };
    }

    /**
     * Converts content record to data transfer object
     *
     * @param content content entity to be converted
     * @return converted data transfer object
     */
    protected T convert(Content content) {
        final T entity;
        try {
            entity = typeClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to instantiate entity by default constructor: %s".formatted(e.toString()));
            return null;
        }

        for (Field field : typeClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ContentId.class)) {
                // set new object id from Content node
                setId(entity, content.getId(), field);
            } else if (field.isAnnotationPresent(ContentMapping.class)) {
                mapEntityField(content, entity, field);
            }
        }
        return entity;
    }

    private void mapEntityField(Content content, T entity, Field field) {
        // unbox from serializable and set object value from attribute
        Object attribute = content.getAttribute(field.getAnnotation(ContentMapping.class).value());
        final Class<?> targetClass = ofNullable(attribute)
                .map(Object::getClass)
                .map(Class::getSuperclass)
                .orElse(null);
        if (targetClass != null && Objects.equals(field.getType(), targetClass)) {
            try {
                final Object newInstance = targetClass.getConstructor().newInstance();
                for (Field newInstanceField : targetClass.getDeclaredFields()) {
                    setValue(newInstance, newInstanceField, getValue(attribute, newInstanceField));
                }
                attribute = newInstance;
            } catch (NoSuchMethodException e) {
                log.error("Class %s has no default constructor".formatted(targetClass.getTypeName()));
                attribute = null;
            } catch (InvocationTargetException | InstantiationException e) {
                log.error("Failed to create new instance of %s: %s".formatted(targetClass.getTypeName(), e.toString()));
                attribute = null;
            } catch (IllegalAccessException e) {
                log.error("Constructor of class %s has private access: %s".formatted(targetClass.getTypeName(), e.toString()));
                attribute = null;
            }
        }
        setValue(entity, field, attribute);
    }

    private Object getValue(Object target, Field field) {
        Object value = null;
        try {
            value = FieldUtils.readField(field, target, true);
        } catch (IllegalAccessException e) {
            log.error("Failed to get value from object field: %s".formatted(e.toString()));
        }

        return value;
    }

    private void setValue(Object target, Field field, Object value) {
        if (target != null && field != null) {
            try {
                FieldUtils.writeField(field, target, value, true);
            } catch (SecurityException | IllegalAccessException e) {
                log.error("Failed to set field %s value for target %s: %s"
                        .formatted(field.getName(), target.getClass().getTypeName(), e.toString()));
            } catch (IllegalArgumentException e) {
                log.error("Attempt to set value of type %s to field of type %s".formatted(value.getClass().getTypeName(), field.getType().getTypeName()));
            }
        }

    }

    @NotNull
    private String getValueClassName(Object value) {
        return ofNullable(value)
                .map(Object::getClass)
                .map(Class::getTypeName)
                .orElse("NULL");
    }

    private Long getIdValue(Object target, Field field) {
        Object id = null;
        try {
            id = FieldUtils.readField(field, target, true);
        } catch (IllegalAccessException e) {
            log.error("Failed to read field %s value from object %s, exception: %s"
                    .formatted(field.getName(), target.toString(), e.toString()));
        }
        return id instanceof Long ? (Long) id : null;
    }

    private void setId(Object entity, Long id, Field field) {
        try {
            FieldUtils.writeField(field, entity, id, true);
        } catch (SecurityException | IllegalAccessException e) {
            log.error("Failed to update identifier for entity %d: %s".formatted(id, e.toString()));
        }
    }

    private Class getGenericParameterClass(Class<?> actualClass) {
        return (Class) ((ParameterizedType) actualClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
