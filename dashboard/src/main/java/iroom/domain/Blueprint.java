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
@Table(name = "Blueprint_table")
@Data
//<<< DDD / Aggregate Root
public class Blueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String blueprintUrl;

    private Integer floor;

    private Double width;

    private Double height;

    public static BlueprintRepository repository() {
        BlueprintRepository blueprintRepository = DashboardApplication.applicationContext.getBean(
            BlueprintRepository.class
        );
        return blueprintRepository;
    }

    //<<< Clean Arch / Port Method
    public void registerBlueprint(
        RegisterBlueprintCommand registerBlueprintCommand
    ) {
        //implement business logic here:

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void editBlueprint(EditBlueprintCommand editBlueprintCommand) {
        //implement business logic here:

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void deleteBlueprint() {
        //implement business logic here:

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
