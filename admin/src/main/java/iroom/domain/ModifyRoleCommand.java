package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class ModifyRoleCommand {

    private Long id;
    private String role;
}
