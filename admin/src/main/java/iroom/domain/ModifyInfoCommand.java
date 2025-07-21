package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class ModifyInfoCommand {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String phone;
}
