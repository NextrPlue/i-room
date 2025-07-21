package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class WorkerExited extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Date outDate;

    public WorkerExited(WorkerManagement aggregate) {
        super(aggregate);
    }

    public WorkerExited() {
        super();
    }
}
//>>> DDD / Domain Event
