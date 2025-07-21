package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class EditDangerAreaCommand {

    private Long id;
    private Long blueprintId;
    private String location;
    private Double width;
    private Double height;
}
