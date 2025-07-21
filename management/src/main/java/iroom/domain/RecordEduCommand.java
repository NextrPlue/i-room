package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class RecordEduCommand {

    private Long id;
    private Long workerId;
    private String name;
    private String certUrl;
    private Date eduDate;
}
