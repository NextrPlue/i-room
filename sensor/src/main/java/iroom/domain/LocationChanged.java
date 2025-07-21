package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class LocationChanged extends AbstractEvent {

    private Long id;
    private Long workerId;
    private String workerLocation;

    public LocationChanged(WorkerHealth aggregate) {
        super(aggregate);
    }

    public LocationChanged() {
        super();
    }
}
//>>> DDD / Domain Event
