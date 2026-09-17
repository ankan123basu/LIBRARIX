package com.librarix.dto;

import com.librarix.model.enums.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCreateRequest {

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author/Brand is required")
    private String authorOrBrand;

    private String description;

    private String coverImageUrl;

    @NotNull(message = "Resource type is required")
    private ResourceType type;

    @Min(value = 1, message = "Total quantity must be at least 1")
    private int totalQuantity;

    @NotBlank(message = "Location is required")
    private String location;

    private List<String> tags;
}
