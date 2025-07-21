package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class EditBlueprintCommand {

    private Long id;
    private String blueprintUrl;
    private Integer floor;
    private Double width;
    private Double height;
}
