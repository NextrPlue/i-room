package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class DangerAreaModified extends AbstractEvent {

    private Long id;
    private Long blueprintId;
    private String location;
    private Double width;
    private Double height;

    public DangerAreaModified(DangerArea aggregate) {
        super(aggregate);
    }

    public DangerAreaModified() {
        super();
    }
}
//>>> DDD / Domain Event
