package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class RegisterDangerAreaCommand {

    private Long id;
    private Long blueprintId;
    private String location;
    private Double width;
    private Double height;
}
