package com.librarix.dto;

import com.librarix.model.enums.ResourceStatus;
import com.librarix.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {
    private String id;
    private String barcode;
    private String title;
    private String authorOrBrand;
    private String description;
    private String coverImageUrl;
    private ResourceType type;
    private int totalQuantity;
    private int availableQuantity;
    private String location;
    private List<String> tags;
    private ResourceStatus status;
    private long activeQueueCount;
    private Instant createdAt;
}
