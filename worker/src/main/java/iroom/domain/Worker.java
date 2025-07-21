package iroom.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import iroom.WorkerApplication;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Worker_table")
@Data
//<<< DDD / Aggregate Root
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String email;

    private String password;

    private String phone;

    private String bloodType;

    private String gender;

    private Integer age;

    private Float weight;

    private Float height;

    private String workerJob;

    private String workerPosition;

    private String workerDepartment;

    private String faceImageUrl;

    private Date createAt;

    private Date updatedAt;

    public static WorkerRepository repository() {
        WorkerRepository workerRepository = WorkerApplication.applicationContext.getBean(
            WorkerRepository.class
        );
        return workerRepository;
    }

    //<<< Clean Arch / Port Method
    public void login(LoginCommand loginCommand) {
        //implement business logic here:

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void register(RegisterCommand registerCommand) {
        //implement business logic here:

        Registered registered = new Registered(this);
        registered.publishAfterCommit();
    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void edit(EditCommand editCommand) {
        //implement business logic here:

        Edited edited = new Edited(this);
        edited.publishAfterCommit();
    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
