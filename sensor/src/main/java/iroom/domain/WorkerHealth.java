package iroom.domain;

import iroom.domain.LocationChanged;
import iroom.SensorApplication;
import javax.persistence.*;
import java.util.List;
import lombok.Data;
import java.util.Date;
import java.time.LocalDate;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;


@Entity
@Table(name="WorkerHealth_table")
@Data

//<<< DDD / Aggregate Root
public class WorkerHealth  {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    
    
    
private Long id;    
    
    
private Long workerId;    
    
    
private String workerLocation;    
    
    
private Integer heartRate;    
    
    
private Float bodyTemperature;

    @PostPersist
    public void onPostPersist(){


        LocationChanged locationChanged = new LocationChanged(this);
        locationChanged.publishAfterCommit();

    
    }

    public static WorkerHealthRepository repository(){
        WorkerHealthRepository workerHealthRepository = SensorApplication.applicationContext.getBean(WorkerHealthRepository.class);
        return workerHealthRepository;
    }

    public void recordedWorkerLocation(){
        //
    }


//<<< Clean Arch / Port Method
    public void recordWorkerLocation(RecordWorkerLocationCommand recordWorkerLocationCommand){
        
        //implement business logic here:
        

        iroom.external.WorkerHealthQuery workerHealthQuery = new iroom.external.WorkerHealthQuery();
        // workerHealthQuery.set??()        
          = WorkerHealthApplication.applicationContext
            .getBean(iroom.external.Service.class)
            .workerHealth(workerHealthQuery);

    }
//>>> Clean Arch / Port Method
//<<< Clean Arch / Port Method
    public void recordWorkerBody(RecordWorkerBodyCommand recordWorkerBodyCommand){
        
        //implement business logic here:
        


        WorkerBodyRecorded workerBodyRecorded = new WorkerBodyRecorded(this);
        workerBodyRecorded.publishAfterCommit();
    }
//>>> Clean Arch / Port Method



}
//>>> DDD / Aggregate Root
