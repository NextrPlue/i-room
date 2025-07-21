package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class HealthAnomalPublished extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Date occuredAt;
    private String incidentType;
    private Long incidentId;
    private String workerLocation;
    private Long incidentDescription;

    public HealthAnomalPublished(Alarm aggregate) {
        super(aggregate);
    }

    public HealthAnomalPublished() {
        super();
    }
}
//>>> DDD / Domain Event
