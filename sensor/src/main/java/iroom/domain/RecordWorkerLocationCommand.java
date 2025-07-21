package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class RecordWorkerLocationCommand {

    private Long id;
    private Long workerId;
    private String workerLocation;
}
