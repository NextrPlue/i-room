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
@Table(name = "WorkerManagement_table")
@Data
//<<< DDD / Aggregate Root
public class WorkerManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long workerId;

    private Date enterDate;

    private Date outDate;

    public static WorkerManagementRepository repository() {
        WorkerManagementRepository workerManagementRepository = ManagementApplication.applicationContext.getBean(
            WorkerManagementRepository.class
        );
        return workerManagementRepository;
    }

    //<<< Clean Arch / Port Method
    public void enterWorker(EnterWorkerCommand enterWorkerCommand) {
        //implement business logic here:

        WorkerEntered workerEntered = new WorkerEntered(this);
        workerEntered.publishAfterCommit();
    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void exitWorker(ExitWorkerCommand exitWorkerCommand) {
        //implement business logic here:

        WorkerExited workerExited = new WorkerExited(this);
        workerExited.publishAfterCommit();
    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
