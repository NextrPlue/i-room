package iroom.infra;

import iroom.domain.*;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/dangerAreas")
@Transactional
public class DangerAreaController {

    @Autowired
    DangerAreaRepository dangerAreaRepository;

    @RequestMapping(
        value = "/dangerAreas/registerdangerarea",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public DangerArea registerDangerArea(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody RegisterDangerAreaCommand registerDangerAreaCommand
    ) throws Exception {
        System.out.println(
            "##### /dangerArea/registerDangerArea  called #####"
        );
        DangerArea dangerArea = new DangerArea();
        dangerArea.registerDangerArea(registerDangerAreaCommand);
        dangerAreaRepository.save(dangerArea);
        return dangerArea;
    }

    @RequestMapping(
        value = "/dangerAreas/{id}/editdangerarea",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public DangerArea editDangerArea(
        @PathVariable(value = "id") Long id,
        @RequestBody EditDangerAreaCommand editDangerAreaCommand,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /dangerArea/editDangerArea  called #####");
        Optional<DangerArea> optionalDangerArea = dangerAreaRepository.findById(
            id
        );

        optionalDangerArea.orElseThrow(() -> new Exception("No Entity Found"));
        DangerArea dangerArea = optionalDangerArea.get();
        dangerArea.editDangerArea(editDangerAreaCommand);

        dangerAreaRepository.save(dangerArea);
        return dangerArea;
    }

    @RequestMapping(
        value = "/dangerAreas/{id}/deletedangerarea",
        method = RequestMethod.DELETE,
        produces = "application/json;charset=UTF-8"
    )
    public DangerArea deleteDangerArea(
        @PathVariable(value = "id") Long id,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /dangerArea/deleteDangerArea  called #####");
        Optional<DangerArea> optionalDangerArea = dangerAreaRepository.findById(
            id
        );

        optionalDangerArea.orElseThrow(() -> new Exception("No Entity Found"));
        DangerArea dangerArea = optionalDangerArea.get();
        dangerArea.deleteDangerArea();

        dangerAreaRepository.delete(dangerArea);
        return dangerArea;
    }
}
//>>> Clean Arch / Inbound Adaptor
