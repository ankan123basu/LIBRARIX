package com.librarix.dto;

import com.librarix.model.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueJoinRequest {
    private UrgencyLevel urgencyLevel;
}
