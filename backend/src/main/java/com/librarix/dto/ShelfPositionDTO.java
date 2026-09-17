package com.librarix.dto;

import com.librarix.model.enums.ResourceStatus;
import com.librarix.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShelfPositionDTO {
    private String resourceId;
    private String barcode;
    private String title;
    private String authorOrBrand;
    private String description;
    private String coverImageUrl;
    private ResourceType type;
    private ResourceStatus status;
    private int clusterId;
    private String aisleId;
    private int shelfLevel; // 1 = Bottom, 2 = Eye-Level (1.5m), 3 = Top
    private double posX;
    private double posY;
    private double posZ;
    private String spineColorHex;
    private boolean isEyeLevel;
}
