package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class RegisterCommand {

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
}
