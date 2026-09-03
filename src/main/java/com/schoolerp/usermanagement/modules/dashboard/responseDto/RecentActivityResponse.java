package com.schoolerp.usermanagement.modules.dashboard.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {

    private String type;

    private String message;

    private LocalDateTime timestamp;
}
