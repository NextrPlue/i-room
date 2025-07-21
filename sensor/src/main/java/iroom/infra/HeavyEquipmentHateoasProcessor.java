package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class HeavyEquipmentHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<HeavyEquipment>> {

    @Override
    public EntityModel<HeavyEquipment> process(
        EntityModel<HeavyEquipment> model
    ) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/register")
                .withRel("register")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/locate")
                .withRel("locate")
        );

        return model;
    }
}
