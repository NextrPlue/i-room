package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class 위험요소접근정보발행됨 extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Date occuredAt;
    private String incidentType;
    private Long incidentId;
    private String workerLocation;
    private Long incidentDescription;

    public 위험요소접근정보발행됨(Alarm aggregate) {
        super(aggregate);
    }

    public 위험요소접근정보발행됨() {
        super();
    }
}
//>>> DDD / Domain Event
