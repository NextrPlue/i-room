package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class CreateImprovementCommand {

    private Long id;
    private String metricType;
    private Integer metricValue;
}
