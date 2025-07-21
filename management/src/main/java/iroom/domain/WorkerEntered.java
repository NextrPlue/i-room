package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class WorkerEntered extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Date enterDate;

    public WorkerEntered(WorkerManagement aggregate) {
        super(aggregate);
    }

    public WorkerEntered() {
        super();
    }
}
//>>> DDD / Domain Event
