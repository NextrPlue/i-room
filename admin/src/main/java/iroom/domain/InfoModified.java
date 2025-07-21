package iroom.domain;

import iroom.domain.*;
import iroom.infra.AbstractEvent;
import java.time.LocalDate;
import java.util.*;
import lombok.*;

//<<< DDD / Domain Event
@Data
@ToString
public class InfoModified extends AbstractEvent {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;
    private Date createdAt;
    private Date updatedAt;

    public InfoModified(Admin aggregate) {
        super(aggregate);
    }

    public InfoModified() {
        super();
    }
}
//>>> DDD / Domain Event
