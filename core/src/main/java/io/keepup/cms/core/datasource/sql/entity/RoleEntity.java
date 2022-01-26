package io.keepup.cms.core.datasource.sql.entity;

/**
 * Projection interface. Used to get rid of MappingException while trying to get Flux with user roles as String objects
 *
 * @author Fedor Sergeev
 * @since 2.0.0
 */
public interface RoleEntity {
    String getRole();
}