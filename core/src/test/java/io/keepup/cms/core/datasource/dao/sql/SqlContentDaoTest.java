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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    @Mock
    CacheAdapter adapter;

    private static void assertEmpty(List<Content> result) {
        assertTrue(result.isEmpty());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sqlContentDao = new SqlContentDao(reactiveNodeEntityRepository,
                reactiveNodeAttributeEntityRepository,
                objectMapper,
                manager,
                adapter);
    }

    @Test
    void updateContentAttributeWithMappingException() throws JsonProcessingException {
        var nodeAttributeEntity = new NodeAttributeEntity(1L, "key", "oldValue");
        Mockito.when(objectMapper.writeValueAsBytes(ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("Unit test exception"){});
        Mockito.when(reactiveNodeAttributeEntityRepository.findByContentIdAndAttributeKey(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
                .thenReturn(Mono.just(nodeAttributeEntity));
        Mockito.when(reactiveNodeAttributeEntityRepository.save(ArgumentMatchers.any())).thenReturn(Mono.just(new NodeAttributeEntity()));
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
}