package iroom.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import iroom.ManagementApplication;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "WorkerEdu_table")
@Data
//<<< DDD / Aggregate Root
public class WorkerEdu {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long workerId;

    private String name;

    private String certUrl;

    private Date eduDate;

    public static WorkerEduRepository repository() {
        WorkerEduRepository workerEduRepository = ManagementApplication.applicationContext.getBean(
            WorkerEduRepository.class
        );
        return workerEduRepository;
    }

    //<<< Clean Arch / Port Method
    public void recordEdu(RecordEduCommand recordEduCommand) {
        //implement business logic here:

        EduRecorded eduRecorded = new EduRecorded(this);
        eduRecorded.publishAfterCommit();
    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
