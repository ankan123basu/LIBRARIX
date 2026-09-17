package com.librarix.shelf;

import com.librarix.dto.ShelfPositionDTO;
import com.librarix.model.Resource;
import com.librarix.model.enums.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShelfSlottingOptimizerTest {

    @Test
    void testOptimize3DLayout_EyeLevelPlacement() {
        ShelfSlottingOptimizer optimizer = new ShelfSlottingOptimizer();

        Resource popularBook = Resource.builder()
                .id("res-pop")
                .barcode("BK-POP")
                .title("Popular Book")
                .type(ResourceType.BOOK)
                .totalQuantity(5)
                .availableQuantity(5)
                .build();

        Resource rareBook = Resource.builder()
                .id("res-rare")
                .barcode("BK-RARE")
                .title("Rare Book")
                .type(ResourceType.BOOK)
                .totalQuantity(1)
                .availableQuantity(1)
                .build();

        Map<String, Integer> clusterMap = Map.of("res-pop", 1, "res-rare", 1);
        Map<String, Long> loanCounts = Map.of("res-pop", 50L, "res-rare", 1L);

        List<ShelfPositionDTO> layout = optimizer.optimize3DLayout(
                List.of(popularBook, rareBook),
                clusterMap,
                loanCounts
        );

        assertEquals(2, layout.size());

        ShelfPositionDTO popDto = layout.stream().filter(p -> p.getResourceId().equals("res-pop")).findFirst().get();
        assertTrue(popDto.isEyeLevel());
        assertEquals(1.5, popDto.getPosY());
        assertEquals(2, popDto.getShelfLevel());
    }
}
