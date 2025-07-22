package com.iroom.management.dto.response;

import com.iroom.management.entity.WorkerManagement;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerManagementResponseDto {
    private Long id;
    private Long workerId;
    private Date enterDate;
    private Date outDate;

    public WorkerManagementResponseDto(WorkerManagement entity){
        this.id=entity.getId();
        this.workerId=entity.getWorkerId();
        this.enterDate=entity.getEnterDate();
        this.outDate=entity.getOutDate();
    }
}
