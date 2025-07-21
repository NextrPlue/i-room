package iroom.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import iroom.AlarmApplication;
import iroom.domain.HealthAnomalPublished;
import iroom.domain.IdleWorkerPublished;
import iroom.domain.NoPpePublished;
import iroom.domain.위험요소접근정보발행됨;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Alarm_table")
@Data
//<<< DDD / Aggregate Root
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long workerId;

    private Date occuredAt;

    private String incidentType;

    private Long incidentId;

    private Long incidentDescription;

    public static AlarmRepository repository() {
        AlarmRepository alarmRepository = AlarmApplication.applicationContext.getBean(
            AlarmRepository.class
        );
        return alarmRepository;
    }

    //<<< Clean Arch / Port Method
    public static void 유휴근로자발생(AlertedIdle alertedIdle) {
        //implement business logic here:

        /** Example 1:  new item 
        Alarm alarm = new Alarm();
        repository().save(alarm);

        IdleWorkerPublished idleWorkerPublished = new IdleWorkerPublished(alarm);
        idleWorkerPublished.publishAfterCommit();
        */

        /** Example 2:  finding and process
        

        repository().findById(alertedIdle.get???()).ifPresent(alarm->{
            
            alarm // do something
            repository().save(alarm);

            IdleWorkerPublished idleWorkerPublished = new IdleWorkerPublished(alarm);
            idleWorkerPublished.publishAfterCommit();

         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
