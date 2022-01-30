package io.keepup.plugins.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.boot.KeepupApplication;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.cache.KeepupCacheConfiguration;
import io.keepup.cms.core.config.DataSourceConfiguration;
import io.keepup.cms.core.config.R2dbcConfiguration;
import io.keepup.cms.core.config.WebFluxConfig;
import io.keepup.cms.core.datasource.dao.DataSourceFacadeImpl;
import io.keepup.cms.core.datasource.dao.sql.SqlContentDao;
import io.keepup.cms.core.datasource.dao.sql.SqlFileDao;
import io.keepup.cms.core.datasource.dao.sql.SqlUserDao;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveUserEntityRepository;
import io.keepup.plugins.catalog.dao.LayoutEntity;
import io.keepup.plugins.catalog.dao.LayoutEntityRepository;
import io.keepup.plugins.catalog.model.AttributeType;
import io.keepup.plugins.catalog.model.Layout;
import io.keepup.plugins.catalog.model.LayoutApiAttribute;
import io.keepup.plugins.catalog.service.LayoutService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.error;

@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "h2"})
@ContextConfiguration(classes = {
        KeepupApplication.class,
        KeepupCacheConfiguration.class,
        CacheAdapter.class,
        WebFluxConfig.class,
        ReactiveNodeEntityRepository.class,
        ReactiveNodeAttributeEntityRepository.class,
        ReactiveUserEntityRepository.class,
        DataSourceConfiguration.class,
        R2dbcConfiguration.class,
        SqlContentDao.class,
        SqlFileDao.class,
        SqlUserDao.class,
        DataSourceFacadeImpl.class,
        LayoutService.class,
        LayoutEntityRepository.class
})
@TestPropertySource(properties = {
        "keepup.plugins.catalog.enabled=true",
})
@DataR2dbcTest
class LayoutServiceTest {

    private final Log log = LogFactory.getLog(getClass());

    private Layout layout;

    @Mock
    private ObjectMapper mapper;
    @Mock
    private LayoutEntityRepository layoutEntityRepository;

    @Autowired
    private LayoutService layoutService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LayoutEntityRepository reactiveLayoutEntityRepository;

    @BeforeEach
    void setUp() {
        LayoutApiAttribute layoutApiAttribute = new LayoutApiAttribute();
        layoutApiAttribute.setKey("name");
        layoutApiAttribute.setResolve(AttributeType.HTML);
        layoutApiAttribute.setTable(true);
        layoutApiAttribute.setTag("p");
        layoutApiAttribute.setName("object name");

        layout = new Layout();
        layout.setHtml("<p>{{objectName}}</p>");
        layout.setName("testLayout");
        layout.getAttributes().add(layoutApiAttribute);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void get() {
        Layout layout = layoutService.save(this.layout)
                .flatMap(savedLayout -> layoutService.get(savedLayout.getId()))
                .block();
        assertNotNull(layout);
        assertNotNull(layout.getId());
        assertNotNull(layout.getHtml());
        assertNotNull(layout.getAttributes());
        assertFalse(layout.getAttributes().isEmpty());
        assertEquals(1, layout.getAttributes().size());
        assertEquals(this.layout.getAttributes().get(0), layout.getAttributes().get(0));
    }

    @Test
    void save() {
        Layout layout = layoutService.deleteAll()
                .then(layoutService.save(this.layout))
                .block();
        assertNotNull(layout);
        assertNotNull(layout.getId());
        assertNotNull(layout.getHtml());
        assertNotNull(layout.getAttributes());
        assertFalse(layout.getAttributes().isEmpty());
        assertEquals(1, layout.getAttributes().size());
        assertEquals(this.layout.getAttributes().get(0), layout.getAttributes().get(0));
    }

    @Test
    void testWithWrongJsonMapper() throws JsonProcessingException {
        when(mapper.writeValueAsString(any()))
                .thenThrow(new JsonMappingException(() -> log.info("exception"),"test exception"));
        var mockLayoutService = new LayoutService(reactiveLayoutEntityRepository, mapper);
        this.layout.setName(UUID.randomUUID().toString());
        var layout = mockLayoutService.save(this.layout).block();
        assertNotNull(layout);
        assertNotNull(layout.getId());
        assertNotNull(layout.getHtml());
        assertNotNull(layout.getAttributes());
        assertTrue(layout.getAttributes().isEmpty());
    }

    @Test
    void testWithWrongDao() {
        when(layoutEntityRepository.save(any())).thenReturn(Mono.just(wrongEntity()));
        var mockLayoutService = new LayoutService(layoutEntityRepository, objectMapper);
        var layout = mockLayoutService.save(this.layout).block();
        assertNotNull(layout);
        assertNotNull(layout.getId());
        assertNotNull(layout.getHtml());
        assertNotNull(layout.getAttributes());
        assertTrue(layout.getAttributes().isEmpty());
    }

    @Test
    void testWithErrorOnGetRequest() {
        when(layoutEntityRepository.findById(anyLong()))
                .thenReturn(error(new RuntimeException("test error")));
        var mockLayoutService = new LayoutService(layoutEntityRepository, objectMapper);
        try {
            mockLayoutService.get(1L).block();
        } catch (RuntimeException e) {
            assertEquals("test error", e.getMessage());
        }
    }

    @Test
    void delete() {
        var id = new AtomicLong();
        var layout = new Layout();

        layout.setHtml(EMPTY);
        layout.setName(EMPTY);
        Layout deletedLayout = layoutService.save(layout).map(saved -> {
            id.set(saved.getId());
            return layoutService.delete(saved.getId());
        })
                .then(layoutService.get(id.get()))
                .block();

        assertNull(deletedLayout);
    }

    @Test
    void getByNullName() {
        assertNull(layoutService.getByName(null).block());
    }

    @Test
    void getByNullNames() {
        var result = layoutService.getByNames(null).collectList().block();
        var resultByEmptyNames = layoutService.getByNames(Collections.emptyList()).collectList().block();
        assertNotNull(result);
        assertNotNull(resultByEmptyNames);
        assertTrue(result.isEmpty());
        assertTrue(resultByEmptyNames.isEmpty());
    }

    private LayoutEntity wrongEntity() {
        var wrongLayoutEntity = new LayoutEntity();
        wrongLayoutEntity.setId(2888L);
        wrongLayoutEntity.setHtml(EMPTY);
        wrongLayoutEntity.setName("Wrong attributes");
        wrongLayoutEntity.setAttributes("546279354fg");
        return  wrongLayoutEntity;
    }
}