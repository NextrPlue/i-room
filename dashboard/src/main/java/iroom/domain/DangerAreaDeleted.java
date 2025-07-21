package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class DangerAreaDeleted extends AbstractEvent {

    private Long id;

    public DangerAreaDeleted(DangerArea aggregate) {
        super(aggregate);
    }

    public DangerAreaDeleted() {
        super();
    }
}
//>>> DDD / Domain Event
