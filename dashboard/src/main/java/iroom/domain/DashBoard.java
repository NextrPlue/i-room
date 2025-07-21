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
@Table(name = "DashBoard_table")
@Data
//<<< DDD / Aggregate Root
public class DashBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String metricType;

    private Integer metricValue;

    private Date recordedAt;

    public static DashBoardRepository repository() {
        DashBoardRepository dashBoardRepository = DashboardApplication.applicationContext.getBean(
            DashBoardRepository.class
        );
        return dashBoardRepository;
    }

    //<<< Clean Arch / Port Method
    public void exportReport(ExportReportCommand exportReportCommand) {
        //implement business logic here:

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void createImprovement(
        CreateImprovementCommand createImprovementCommand
    ) {
        //implement business logic here:

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
