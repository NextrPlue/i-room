package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class RecordWorkerBodyCommand {

    private Long id;
    private Long workerId;
    private Integer heartRate;
    private Float bodyTemperature;
}
