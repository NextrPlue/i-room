package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class WorkerBodyRecorded extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Integer heartRate;
    private Float bodyTemperature;

    public WorkerBodyRecorded(WorkerHealth aggregate) {
        super(aggregate);
    }

    public WorkerBodyRecorded() {
        super();
    }
}
//>>> DDD / Domain Event
