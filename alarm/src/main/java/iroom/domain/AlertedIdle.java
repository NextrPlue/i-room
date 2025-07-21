package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class AlertedIdle extends AbstractEvent {

    private Long id;
    private Long workerId;
    private Integer heartRate;
    private Float bodyTemperature;
    private String workerLocation;
    private Long biometricScore;
    private String idleLevel;
    private Date analyzedAt;
}
