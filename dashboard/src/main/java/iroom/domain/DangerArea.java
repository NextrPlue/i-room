package iroom.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import iroom.DashboardApplication;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "DangerArea_table")
@Data
//<<< DDD / Aggregate Root
public class DangerArea {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long blueprintId;

    private String location;

    private Double width;

    private Double height;

    public static DangerAreaRepository repository() {
        DangerAreaRepository dangerAreaRepository = DashboardApplication.applicationContext.getBean(
            DangerAreaRepository.class
        );
        return dangerAreaRepository;
    }

    //<<< Clean Arch / Port Method
    public void registerDangerArea(
        RegisterDangerAreaCommand registerDangerAreaCommand
    ) {
        //implement business logic here:

        DangerAreaRegistered dangerAreaRegistered = new DangerAreaRegistered(
            this
        );
        dangerAreaRegistered.publishAfterCommit();
        DangerAreaRegistered dangerAreaRegistered = new DangerAreaRegistered(
            this
        );
        dangerAreaRegistered.publishAfterCommit();
    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void editDangerArea(EditDangerAreaCommand editDangerAreaCommand) {
        //implement business logic here:

        DangerAreaModified dangerAreaModified = new DangerAreaModified(this);
        dangerAreaModified.publishAfterCommit();
    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void deleteDangerArea() {
        //implement business logic here:

        DangerAreaDeleted dangerAreaDeleted = new DangerAreaDeleted(this);
        dangerAreaDeleted.publishAfterCommit();
    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
