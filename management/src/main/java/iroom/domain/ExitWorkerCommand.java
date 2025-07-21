package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class ExitWorkerCommand {

    private Long id;
    private Long workerId;
    private Date outDate;
}
