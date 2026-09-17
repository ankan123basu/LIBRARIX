package com.librarix.shelf;

import com.librarix.dto.ShelfPositionDTO;
import com.librarix.model.Resource;
import com.librarix.model.enums.ResourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Addition 4 — Algorithm 4: 3D Shelf-Slotting Optimizer
 *
 * Greedy warehouse-slotting algorithm:
 * 1. Computes popularity score per item from loan frequency and active demand.
 * 2. Groups items into affinity clusters (from CoBorrowGraphService).
 * 3. Assigns High-Popularity items (Top 30%) to Eye-Level shelf height (y = 1.5m).
 * 4. Slots cluster-adjacent books side by side along X-axis aisles (spacing = 0.15m).
 */
@Slf4j
@Service
public class ShelfSlottingOptimizer {

    private static final double EYE_LEVEL_Y = 1.5;
    private static final double BOTTOM_LEVEL_Y = 0.4;
    private static final double TOP_LEVEL_Y = 2.4;
    private static final double BOOK_SPACING_X = 0.18;
    private static final int ITEMS_PER_SHELF_ROW = 8;

    public List<ShelfPositionDTO> optimize3DLayout(List<Resource> resources,
                                                   Map<String, Integer> clusterMap,
                                                   Map<String, Long> loanCountMap) {
        if (resources == null || resources.isEmpty()) {
            return List.of();
        }

        // 1. Calculate Popularity Scores
        Map<String, Double> popularityScores = new HashMap<>();
        for (Resource res : resources) {
            long loans = loanCountMap.getOrDefault(res.getId(), 0L);
            double score = (loans * 2.0) + (res.getAvailableQuantity() * 0.5);
            popularityScores.put(res.getId(), score);
        }

        // Find top 30% threshold for Eye-Level placement
        List<Double> scores = new ArrayList<>(popularityScores.values());
        Collections.sort(scores, Collections.reverseOrder());
        int top30Index = (int) Math.ceil(scores.size() * 0.3);
        double eyeLevelThreshold = (scores.isEmpty() || top30Index == 0) ? 0.0 : scores.get(Math.min(top30Index, scores.size() - 1));

        // 2. Sort Resources by Cluster ID then Popularity Score DESC
        List<Resource> sortedResources = new ArrayList<>(resources);
        sortedResources.sort((r1, r2) -> {
            int c1 = clusterMap.getOrDefault(r1.getId(), 1);
            int c2 = clusterMap.getOrDefault(r2.getId(), 1);
            if (c1 != c2) {
                return Integer.compare(c1, c2);
            }
            return Double.compare(popularityScores.getOrDefault(r2.getId(), 0.0), popularityScores.getOrDefault(r1.getId(), 0.0));
        });

        // 3. Compute 3D Coordinates (Aisle, Shelf Level, X, Y, Z)
        List<ShelfPositionDTO> positions = new ArrayList<>();
        int currentAisle = 1;
        int currentSlotInRow = 0;
        int currentLevelIndex = 2; // Start at Eye-Level (Level 2)

        for (Resource res : sortedResources) {
            double popScore = popularityScores.getOrDefault(res.getId(), 0.0);
            boolean isEyeLevel = popScore >= eyeLevelThreshold;

            int level;
            double posY;

            if (isEyeLevel) {
                level = 2;
                posY = EYE_LEVEL_Y;
            } else if (currentSlotInRow % 2 == 0) {
                level = 1;
                posY = BOTTOM_LEVEL_Y;
            } else {
                level = 3;
                posY = TOP_LEVEL_Y;
            }

            double posX = (currentSlotInRow - (ITEMS_PER_SHELF_ROW / 2.0)) * BOOK_SPACING_X;
            double posZ = (currentAisle - 1) * 2.5; // Aisles spaced 2.5m apart

            String spineColor = getSpineColorHex(res.getType(), isEyeLevel);

            positions.add(ShelfPositionDTO.builder()
                    .resourceId(res.getId())
                    .barcode(res.getBarcode())
                    .title(res.getTitle())
                    .authorOrBrand(res.getAuthorOrBrand())
                    .description(res.getDescription())
                    .coverImageUrl(res.getCoverImageUrl())
                    .type(res.getType())
                    .status(res.getStatus())
                    .clusterId(clusterMap.getOrDefault(res.getId(), 1))
                    .aisleId("AISLE-" + String.format("%02d", currentAisle))
                    .shelfLevel(level)
                    .posX(posX)
                    .posY(posY)
                    .posZ(posZ)
                    .spineColorHex(spineColor)
                    .isEyeLevel(isEyeLevel)
                    .build());

            currentSlotInRow++;
            if (currentSlotInRow >= ITEMS_PER_SHELF_ROW) {
                currentSlotInRow = 0;
                currentAisle++;
            }
        }

        log.info("Optimized 3D Shelf Slotting layout for {} assets across {} aisles.", positions.size(), currentAisle);
        return positions;
    }

    private String getSpineColorHex(ResourceType type, boolean isEyeLevel) {
        if (type == ResourceType.HARDWARE) return "#a855f7"; // Purple
        if (type == ResourceType.LAB_KIT) return "#10b981"; // Emerald
        if (type == ResourceType.SEMINAR_ROOM) return "#f59e0b"; // Amber
        return isEyeLevel ? "#6366f1" : "#38bdf8"; // Indigo / Sky
    }
}
