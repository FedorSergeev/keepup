package io.keepup.plugins.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.plugins.catalog.dao.LayoutEntity;
import io.keepup.plugins.catalog.dao.LayoutEntityRepository;
import io.keepup.plugins.catalog.model.Layout;
import io.keepup.plugins.catalog.model.LayoutApiAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;
import static reactor.core.publisher.Mono.empty;

/**
 * Business layer for work with {@link Layout}
 *
 * @author Fedor Sergeev
 * @since 1.8
 */
@Service
@ConditionalOnProperty(prefix = "keepup.plugins.catalog", name = "enabled", havingValue = "true")
public class LayoutService {
    private static final String FOUND_LAYOUT_LOG = "Found layout id = %d name = %s";
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
     * @return       Publisher signaling when layout is saved
     */
    public Mono<Layout> save(Layout layout) {
        log.debug("Received request to save Layout entity with id = %d and name = %s"
                .formatted(layout.getId(), layout.getName()));
        String layoutAttributes;
        var layoutEntity = new LayoutEntity();
        layoutEntity.setId(layout.getId());
        layoutEntity.setName(layout.getName());
        layoutEntity.setHtml(layout.getHtml());
        layoutEntity.setBreadCrumbName(layout.getBreadCrumbElementName());

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
        log.info("Looking for Layout entity with id = %d".formatted(id));
        return layoutEntityRepository.findById(id)
                .doOnNext(entity -> log.debug(FOUND_LAYOUT_LOG.formatted(entity.getId(), entity.getName())))
                .switchIfEmpty(empty())
                .onErrorResume(this::logErrorAndReturnMono)
                .map(this::buildLayoutApiDto);
    }

    /**
     * Finds {@link Layout} by it's name which must be unique
     *
     * @param name {@link Layout} name
     * @return     publisher for found {@link Layout} object
     */
    public Mono<Layout> getByName(String name) {
        if (name == null) {
            log.debug("Empty name parameter");
            return Mono.empty();
        }
        log.debug("Looking for Layout entity with name = %s".formatted(name));
        return layoutEntityRepository.findByName(name)
                .doOnNext(entity -> log.debug(FOUND_LAYOUT_LOG.formatted(entity.getId(), entity.getName())))
                .switchIfEmpty(empty())
                .onErrorResume(this::logErrorAndReturnMono)
                .map(this::buildLayoutApiDto);
    }

    /**
     * Finds {@link Layout} entities according to their names. Will return empty Flux if names is null or empty
     *
     * @param names {@link Layout} names
     * @return      publisher for found {@link Layout} objects
     */
    public Flux<Layout> getByNames(Iterable<String> names) {
        if (names == null
         || StreamSupport.stream(names.spliterator(), false).count() == 0) {
            log.debug("No names given for fetching the Layout entities");
            return Flux.empty();
        }
        var stringBuilder = new StringBuilder();
        names.forEach(name -> stringBuilder.append(name).append(" "));
        log.debug("Looking for Layout entities with names in [ %s]".formatted(stringBuilder.toString()));
        return layoutEntityRepository.findByNames(names)
                .doOnNext(entity -> log.debug(FOUND_LAYOUT_LOG.formatted(entity.getId(), entity.getName())))
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
        resultLayout.setBreadCrumbElementName(entity.getBreadCrumbName());

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
