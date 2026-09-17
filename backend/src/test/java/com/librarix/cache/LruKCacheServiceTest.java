package com.librarix.cache;

import com.librarix.dto.ResourceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LruKCacheServiceTest {

    private LruKCacheService cacheService;

    @BeforeEach
    void setUp() {
        // Capacity 2, K = 2
        cacheService = new LruKCacheService(2, 2);
    }

    @Test
    void testLruKCacheEviction() {
        ResourceDTO r1 = ResourceDTO.builder().id("1").title("Book 1").build();
        ResourceDTO r2 = ResourceDTO.builder().id("2").title("Book 2").build();
        ResourceDTO r3 = ResourceDTO.builder().id("3").title("Book 3").build();

        cacheService.put("1", r1);
        cacheService.put("2", r2);

        // Access "1" twice so its K-distance is fresh
        cacheService.get("1");
        cacheService.get("1");

        // Inserting "3" should evict "2" (since "2" has fewer accesses than "1")
        cacheService.put("3", r3);

        assertNotNull(cacheService.get("1"));
        assertNull(cacheService.get("2"));
        assertNotNull(cacheService.get("3"));
    }
}
