package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class AdminHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Admin>> {

    @Override
    public EntityModel<Admin> process(EntityModel<Admin> model) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/register")
                .withRel("register")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/modifyrole")
                .withRel("modifyrole")
        );

        return model;
    }
}
