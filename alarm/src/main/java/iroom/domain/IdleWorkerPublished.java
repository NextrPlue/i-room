package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class IdleWorkerPublished extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Date occuredAt;
    private String incidentType;
    private Long incidentId;
    private String workerLocation;
    private Long incidentDescription;

    public IdleWorkerPublished(Alarm aggregate) {
        super(aggregate);
    }

    public IdleWorkerPublished() {
        super();
    }
}
//>>> DDD / Domain Event
