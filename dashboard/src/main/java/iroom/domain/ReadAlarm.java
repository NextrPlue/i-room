package iroom.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

//<<< EDA / CQRS
@Entity
@Table(name = "ReadAlarm_table")
@Data
public class ReadAlarm {

    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private Long workerId;
    private Date occuredAt;
    private String incidentType;
    private String workerLocation;
    private String description;
}
