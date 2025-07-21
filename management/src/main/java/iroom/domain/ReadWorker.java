package iroom.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

//<<< EDA / CQRS
@Entity
@Table(name = "ReadWorker_table")
@Data
public class ReadWorker {

    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private Long workerId;
    private String name;
    private String bloodType;
    private String workerGender;
    private String workerPosition;
    private String workerJob;
    private String workerDepartment;
    private Integer workerAge;
    private Float workerHeight;
    private Float workerWeight;
    private String faceImageUrl;
}
