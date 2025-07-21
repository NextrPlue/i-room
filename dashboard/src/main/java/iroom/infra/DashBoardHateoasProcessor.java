package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class DashBoardHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<DashBoard>> {

    @Override
    public EntityModel<DashBoard> process(EntityModel<DashBoard> model) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/exportreport")
                .withRel("exportreport")
        );
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/createimprovement"
                )
                .withRel("createimprovement")
        );

        return model;
    }
}
