package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class Located extends AbstractEvent {

    private Long id;
    private String name;
    private String type;
    private String location;
    private Double radius;

    public Located(HeavyEquipment aggregate) {
        super(aggregate);
    }

    public Located() {
        super();
    }
}
//>>> DDD / Domain Event
