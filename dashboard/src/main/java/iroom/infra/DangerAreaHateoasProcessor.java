package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class DangerAreaHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<DangerArea>> {

    @Override
    public EntityModel<DangerArea> process(EntityModel<DangerArea> model) {
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/registerdangerarea"
                )
                .withRel("registerdangerarea")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/editdangerarea")
                .withRel("editdangerarea")
        );
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/deletedangerarea"
                )
                .withRel("deletedangerarea")
        );

        return model;
    }
}
