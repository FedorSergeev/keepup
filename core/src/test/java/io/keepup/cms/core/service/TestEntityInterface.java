package io.keepup.cms.core.service;

import java.io.Serializable;

/**
 * Interface for testing {@link EntityOperationServiceBase} work with subclasses and implementations
 */
public interface TestEntityInterface extends Serializable {
    Long getId();
    String getName();
}
