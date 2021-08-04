package com.ibm.sec.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStatusDto {
    private Long taskStatusId;
    private String taskName;
    private String status;
    private String updatedDateTime;
}
