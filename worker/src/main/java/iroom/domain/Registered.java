package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class Registered extends AbstractEvent {

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

    public Registered(Worker aggregate) {
        super(aggregate);
    }

    public Registered() {
        super();
    }
}
//>>> DDD / Domain Event
