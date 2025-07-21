package iroom.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import iroom.SensorApplication;
import iroom.domain.Located;
import iroom.domain.Registered;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "HeavyEquipment_table")
@Data
//<<< DDD / Aggregate Root
public class HeavyEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String location;

    private Double radius;

    @PostPersist
    public void onPostPersist() {
        Registered registered = new Registered(this);
        registered.publishAfterCommit();

        Located located = new Located(this);
        located.publishAfterCommit();
    }

    public static HeavyEquipmentRepository repository() {
        HeavyEquipmentRepository heavyEquipmentRepository = SensorApplication.applicationContext.getBean(
            HeavyEquipmentRepository.class
        );
        return heavyEquipmentRepository;
    }

    //<<< Clean Arch / Port Method
    public void register(RegisterCommand registerCommand) {
        //implement business logic here:

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void locate(LocateCommand locateCommand) {
        //implement business logic here:

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
