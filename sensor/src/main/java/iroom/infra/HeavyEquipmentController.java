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
// @RequestMapping(value="/heavyEquipments")
@Transactional
public class HeavyEquipmentController {

    @Autowired
    HeavyEquipmentRepository heavyEquipmentRepository;

    @RequestMapping(
        value = "/heavyEquipments/register",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public HeavyEquipment register(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody RegisterCommand registerCommand
    ) throws Exception {
        System.out.println("##### /heavyEquipment/register  called #####");
        HeavyEquipment heavyEquipment = new HeavyEquipment();
        heavyEquipment.register(registerCommand);
        heavyEquipmentRepository.save(heavyEquipment);
        return heavyEquipment;
    }

    @RequestMapping(
        value = "/heavyEquipments/{id}/locate",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public HeavyEquipment locate(
        @PathVariable(value = "id") Long id,
        @RequestBody LocateCommand locateCommand,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /heavyEquipment/locate  called #####");
        Optional<HeavyEquipment> optionalHeavyEquipment = heavyEquipmentRepository.findById(
            id
        );

        optionalHeavyEquipment.orElseThrow(() ->
            new Exception("No Entity Found")
        );
        HeavyEquipment heavyEquipment = optionalHeavyEquipment.get();
        heavyEquipment.locate(locateCommand);

        heavyEquipmentRepository.save(heavyEquipment);
        return heavyEquipment;
    }
}
//>>> Clean Arch / Inbound Adaptor
