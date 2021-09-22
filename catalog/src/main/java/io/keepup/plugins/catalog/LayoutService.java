package io.keepup.plugins.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.plugins.catalog.dao.LayoutEntity;
import io.keepup.plugins.catalog.dao.LayoutEntityRepository;
import io.keepup.plugins.catalog.model.LayoutApiAttribute;
import io.keepup.plugins.catalog.model.Layout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static reactor.core.publisher.Mono.empty;

/**
 * Business layer for work with {@link Layout}
 */
@Service
@ConditionalOnProperty(prefix = "keepup.plugins.catalog", name = "enabled", havingValue = "true")
public class LayoutService {
    private final Log log = LogFactory.getLog(getClass());
    private final LayoutEntityRepository layoutEntityRepository;
    private final ObjectMapper objectMapper;

    public LayoutService(LayoutEntityRepository layoutEntityRepository, ObjectMapper objectMapper) {
        this.layoutEntityRepository = layoutEntityRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create or update an existing layout object
     *
     * @param layout object to update
     * @return Publisher signaling when layout is saved
     */
    public Mono<Layout> save(Layout layout) {
        log.debug("Received request to save Layout entity with id = %d and name = %s"
                .formatted(layout.getId(), layout.getName()));
        String layoutAttributes;
        var layoutEntity = new LayoutEntity();
        layoutEntity.setId(layout.getId());
        layoutEntity.setName(layout.getName());
        layoutEntity.setHtml(layout.getHtml());

        try {
            layoutAttributes = objectMapper.writeValueAsString(layout.getAttributes());
            layoutEntity.setAttributes(layoutAttributes);
        } catch (JsonProcessingException e) {
            log.error("Failed to fetch layout %s attributes from data transfer object: %s"
                    .formatted(layout.getName(), e.toString()));
            layoutEntity.setAttributes("[]");
        }

        return layoutEntityRepository.save(layoutEntity)
                .map(this::buildLayoutApiDto);
    }

    /**
     * Finds {@link Layout} by it's identifier
     *
     * @param id primary identifier
     * @return   publisher for found {@link Layout} object
     */
    public Mono<Layout> get(Long id) {
        log.debug("Looking for Layout entity with id = %d".formatted(id));
        return layoutEntityRepository.findById(id)
                .doOnNext(entity -> log.debug("Found layout id = %d name = %s"
                        .formatted(entity.getId(), entity.getName())))
                .switchIfEmpty(empty())
                .onErrorResume(this::logErrorAndReturnMono)
                .map(this::buildLayoutApiDto);
    }

    /**
     * Finds {@link Layout} by it's name witch must be unique
     *
     * @param name {@link Layout} name
     * @return     publisher for found {@link Layout} object
     */
    public Mono<Layout> getByName(String name) {
        log.debug("Looking for Layout entity with name = %s".formatted(name));
        return layoutEntityRepository.findByName(name)
                .doOnNext(entity -> log.debug("Found layout id = %d name = %s"
                        .formatted(entity.getId(), entity.getName())))
                .switchIfEmpty(empty())
                .onErrorResume(this::logErrorAndReturnMono)
                .map(this::buildLayoutApiDto);
    }

    /**
     * Delete {@link Layout} by identifier
     *
     * @param id primary identifier of {@link LayoutEntity}
     * @return   publisher signaling when entity is removed from database
     */
    public Mono<Void> delete(Long id) {
        log.debug("Received request to remove entity by id = %d".formatted(id));
        return layoutEntityRepository.deleteById(id);
    }

    /**
     * Delete all {@link Layout} objects by removing {@link LayoutEntity} objects from database.
     *
     * @return   publisher signaling when all entities are removed from database
     */
    public Mono<Void> deleteAll() {
        log.debug("Received request to remove all layout entities");
        return layoutEntityRepository.deleteAll();
    }

    private Mono<LayoutEntity> logErrorAndReturnMono(Throwable exception) {
        log.error("Exception while looking for layout entity: %s".formatted(exception.toString()));
        return Mono.error(exception);
    }

    private Layout buildLayoutApiDto(LayoutEntity entity) {
        var resultLayout = new Layout();
        resultLayout.setId(entity.getId());
        resultLayout.setName(entity.getName());
        resultLayout.setHtml(entity.getHtml());

        try {
            List<LayoutApiAttribute> attributes = objectMapper.readValue(entity.getAttributes(), new TypeReference<>() {});
            resultLayout.setAttributes(ofNullable(attributes).orElse(new ArrayList<>()));
        } catch (JsonProcessingException e) {
            log.error("Failed to fetch layout %s attributes from data transfer object: %s"
                    .formatted(resultLayout.getName(), e.toString()));
        }

        return resultLayout;
    }
}
