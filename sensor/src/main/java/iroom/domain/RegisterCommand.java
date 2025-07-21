package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class RegisterCommand {

    private Long id;
    private String name;
    private String type;
    private Double radius;
}
