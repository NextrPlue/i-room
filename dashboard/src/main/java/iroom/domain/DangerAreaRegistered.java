package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class DangerAreaRegistered extends AbstractEvent {

    private Long id;
    private Long blueprintId;
    private String location;
    private Double width;
    private Double height;

    public DangerAreaRegistered(DangerArea aggregate) {
        super(aggregate);
    }

    public DangerAreaRegistered() {
        super();
    }
}
//>>> DDD / Domain Event
