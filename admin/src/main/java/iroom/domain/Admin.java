package iroom.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import iroom.AdminApplication;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Admin_table")
@Data
//<<< DDD / Aggregate Root
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String email;

    private String password;

    private String phone;

    private String role;

    private Date createdAt;

    private Date updatedAt;

    public static AdminRepository repository() {
        AdminRepository adminRepository = AdminApplication.applicationContext.getBean(
            AdminRepository.class
        );
        return adminRepository;
    }

    //<<< Clean Arch / Port Method
    public void register(RegisterCommand registerCommand) {
        //implement business logic here:

        Registered registered = new Registered(this);
        registered.publishAfterCommit();
    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public void modifyRole(ModifyRoleCommand modifyRoleCommand) {
        //implement business logic here:

        RoleModified roleModified = new RoleModified(this);
        roleModified.publishAfterCommit();
    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
