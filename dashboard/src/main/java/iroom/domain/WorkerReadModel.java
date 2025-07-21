package iroom.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

//<<< EDA / CQRS
@Entity
@Table(name = "WorkerReadModel_table")
@Data
public class WorkerReadModel {

    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private Long workerId;
    private String workerLocation;
    private Integer heartRate;
}
