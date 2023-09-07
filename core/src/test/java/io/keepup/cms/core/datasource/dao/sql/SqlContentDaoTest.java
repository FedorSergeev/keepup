package io.keepup.cms.core.datasource.dao.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.keepup.cms.core.cache.CacheAdapter;
import io.keepup.cms.core.datasource.sql.entity.NodeAttributeEntity;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeAttributeEntityRepository;
import io.keepup.cms.core.datasource.sql.repository.ReactiveNodeEntityRepository;
import io.keepup.cms.core.persistence.Content;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Additional unit tests
 */
@ExtendWith(MockitoExtension.class)
class SqlContentDaoTest {

    SqlContentDao sqlContentDao;

    @Mock
    ReactiveNodeEntityRepository reactiveNodeEntityRepository;
    @Mock
    ReactiveNodeAttributeEntityRepository reactiveNodeAttributeEntityRepository;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    CacheManager manager;

    CacheAdapter adapter;

    private static void assertEmpty(List<Content> result) {
        assertTrue(result.isEmpty());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new CacheAdapter(manager);
        sqlContentDao = new SqlContentDao(reactiveNodeEntityRepository,
                reactiveNodeAttributeEntityRepository,
                objectMapper,
                manager,
                adapter);
    }

    @Test
    void updateContentAttributeWithMappingException() throws JsonProcessingException {
        var nodeAttributeEntity = new NodeAttributeEntity(1L, "key", "oldValue");
        when(objectMapper.writeValueAsBytes(ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("Unit test exception"){});
        when(reactiveNodeAttributeEntityRepository.findByContentIdAndAttributeKey(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(nodeAttributeEntity));
        when(reactiveNodeAttributeEntityRepository.save(ArgumentMatchers.any())).thenReturn(Mono.just(new NodeAttributeEntity()));
        sqlContentDao.updateContentAttribute(1L, "key", "value")
                .doOnNext(result -> {
                    assertNotNull(result);
                    assertEquals("value", result);
                })
                .block();
    }

    @Test
    void getContentByParentIdAndAttributeValueWithNullParentId() {
        sqlContentDao.getContentByParentIdAndAttributeValue(null, "key", "value")
                .collectList()
                .doOnNext(result -> assertTrue(result.isEmpty()))
                .block();
    }

    @Test
    void getContentByParentIdAndAttributeValueWithNullAttributeKey() {
        sqlContentDao.getContentByParentIdAndAttributeValue(0L, null, "value")
                .collectList()
                .doOnNext(SqlContentDaoTest::assertEmpty)
                .block();
    }

    @Test
    void getContentAttributeWithNotFoundClass() {
        var nodeAttribute = new NodeAttributeEntity();
        nodeAttribute.setAttributeKey("wrongTypeAttribute");
        nodeAttribute.setAttributeValue(new byte[0]);
        nodeAttribute.setJavaClass("NonExistingJavaClass");
        nodeAttribute.setContentId(2000L);
        nodeAttribute.setId(20001L);

        when(reactiveNodeAttributeEntityRepository.findByContentIdAndAttributeKey(2000L, "wrongTypeAttribute"))
        .thenReturn(Mono.just(nodeAttribute));

        sqlContentDao.getContentAttribute(2000L, "wrongTypeAttribute")
                .doOnNext(serializable -> assertNull(serializable))
                .block();
    }

    @Test
    void getListContentAttribute() throws IOException {
        var listAttribute = new ArrayList(Arrays.asList("a", "b", "c"));
        var nodeAttribute = new NodeAttributeEntity();
        nodeAttribute.setAttributeKey("list");
        nodeAttribute.setAttributeValue(new ObjectMapper().writeValueAsBytes(listAttribute));
        nodeAttribute.setJavaClass("java.util.ArrayList");
        nodeAttribute.setContentId(2000L);
        nodeAttribute.setId(20001L);

        when(reactiveNodeAttributeEntityRepository.findByContentIdAndAttributeKey(2000L, "list"))
                .thenReturn(Mono.just(nodeAttribute));
        when(objectMapper.readValue(nodeAttribute.getAttributeValue(), ArrayList.class))
                .thenReturn(listAttribute);

        sqlContentDao.getContentAttribute(2000L, "list")
                .doOnNext(serializable -> {
                    assertNotNull(serializable);
                    assertTrue(serializable instanceof List);
                })
                .block();
    }

    @Test
    void getContentAttributeWithSerializationError() {
        var nodeAttribute = new NodeAttributeEntity();
        nodeAttribute.setAttributeKey("wrongTypeAttribute");
        nodeAttribute.setAttributeValue("String value".getBytes(UTF_8));
        nodeAttribute.setJavaClass("java.lang.Long");
        nodeAttribute.setContentId(2000L);
        nodeAttribute.setId(20001L);

        when(reactiveNodeAttributeEntityRepository.findByContentIdAndAttributeKey(2000L, "wrongTypeAttribute"))
                .thenReturn(Mono.just(nodeAttribute));

        sqlContentDao.getContentAttribute(2000L, "wrongTypeAttribute")
                .doOnNext(serializable -> assertNull(serializable))
                .block();
    }
}