package iroom.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;

@Data
public class LocateCommand {

    private Long id;
    private String location;
}
