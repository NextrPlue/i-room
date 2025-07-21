package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class EnterWorkerCommand {

    private Long id;
    private Long workerId;
    private Date enterDate;
}
