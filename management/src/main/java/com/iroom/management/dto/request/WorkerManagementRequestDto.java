package com.iroom.management.dto.request;

import com.iroom.management.entity.WorkerManagement;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerManagementRequestDto {
    private Long id;
    private Long workerId;
    private Date enterDate;
    private Date outDate;

    public WorkerManagement toEntity() {
        return WorkerManagement.builder()
                .id(this.id)
                .workerId(this.workerId)
                .enterDate(this.enterDate)
                .outDate(this.outDate)
                .build();
    }
}
