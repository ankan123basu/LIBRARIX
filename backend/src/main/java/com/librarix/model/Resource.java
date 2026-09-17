package com.librarix.model;

import com.librarix.model.enums.ResourceStatus;
import com.librarix.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "resources")
public class Resource {

    @Id
    private String id;

    @Indexed(unique = true)
    private String barcode;

    private String title;

    private String authorOrBrand;

    private String description;

    private String coverImageUrl;

    private ResourceType type;

    private int totalQuantity;

    private int availableQuantity;

    private String location;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private ResourceStatus status = ResourceStatus.AVAILABLE;

    @Version
    private Long version;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
