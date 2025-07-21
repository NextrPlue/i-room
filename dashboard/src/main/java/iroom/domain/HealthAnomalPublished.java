package iroom.domain;

import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class HealthAnomalPublished extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Date occuredAt;
    private String incidentType;
    private Long incidentId;
    private String workerLocation;
    private Long incidentDescription;
}
