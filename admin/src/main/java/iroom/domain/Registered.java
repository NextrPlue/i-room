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
    private String role;
    private Date createdAt;
    private Date updatedAt;

    public Registered(Admin aggregate) {
        super(aggregate);
    }

    public Registered() {
        super();
    }
}
//>>> DDD / Domain Event
