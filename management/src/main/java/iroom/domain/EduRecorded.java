package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class EduRecorded extends AbstractEvent {

    private Long id;
    private Long workerId;
    private String name;
    private String certUrl;
    private Date eduDate;

    public EduRecorded(WorkerEdu aggregate) {
        super(aggregate);
    }

    public EduRecorded() {
        super();
    }
}
//>>> DDD / Domain Event
