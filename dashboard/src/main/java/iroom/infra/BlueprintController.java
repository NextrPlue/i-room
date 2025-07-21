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
// @RequestMapping(value="/blueprints")
@Transactional
public class BlueprintController {

    @Autowired
    BlueprintRepository blueprintRepository;

    @RequestMapping(
        value = "/blueprints/{id}/registerblueprint",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public Blueprint registerBlueprint(
        @PathVariable(value = "id") Long id,
        @RequestBody RegisterBlueprintCommand registerBlueprintCommand,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /blueprint/registerBlueprint  called #####");
        Optional<Blueprint> optionalBlueprint = blueprintRepository.findById(
            id
        );

        optionalBlueprint.orElseThrow(() -> new Exception("No Entity Found"));
        Blueprint blueprint = optionalBlueprint.get();
        blueprint.registerBlueprint(registerBlueprintCommand);

        blueprintRepository.save(blueprint);
        return blueprint;
    }

    @RequestMapping(
        value = "/blueprints/{id}/editblueprint",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public Blueprint editBlueprint(
        @PathVariable(value = "id") Long id,
        @RequestBody EditBlueprintCommand editBlueprintCommand,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /blueprint/editBlueprint  called #####");
        Optional<Blueprint> optionalBlueprint = blueprintRepository.findById(
            id
        );

        optionalBlueprint.orElseThrow(() -> new Exception("No Entity Found"));
        Blueprint blueprint = optionalBlueprint.get();
        blueprint.editBlueprint(editBlueprintCommand);

        blueprintRepository.save(blueprint);
        return blueprint;
    }

    @RequestMapping(
        value = "/blueprints/{id}/deleteblueprint",
        method = RequestMethod.DELETE,
        produces = "application/json;charset=UTF-8"
    )
    public Blueprint deleteBlueprint(
        @PathVariable(value = "id") Long id,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /blueprint/deleteBlueprint  called #####");
        Optional<Blueprint> optionalBlueprint = blueprintRepository.findById(
            id
        );

        optionalBlueprint.orElseThrow(() -> new Exception("No Entity Found"));
        Blueprint blueprint = optionalBlueprint.get();
        blueprint.deleteBlueprint();

        blueprintRepository.delete(blueprint);
        return blueprint;
    }
}
//>>> Clean Arch / Inbound Adaptor
