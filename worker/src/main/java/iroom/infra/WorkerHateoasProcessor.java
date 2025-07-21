package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class WorkerHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Worker>> {

    @Override
    public EntityModel<Worker> process(EntityModel<Worker> model) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/login")
                .withRel("login")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/register")
                .withRel("register")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/edit")
                .withRel("edit")
        );

        return model;
    }
}
