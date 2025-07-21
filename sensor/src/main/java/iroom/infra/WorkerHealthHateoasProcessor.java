package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class WorkerHealthHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<WorkerHealth>> {

    @Override
    public EntityModel<WorkerHealth> process(EntityModel<WorkerHealth> model) {
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/recordworkerlocation"
                )
                .withRel("recordworkerlocation")
        );
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/recordworkerbody"
                )
                .withRel("recordworkerbody")
        );

        return model;
    }
}
