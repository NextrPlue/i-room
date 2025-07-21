package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class WorkerManagementHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<WorkerManagement>> {

    @Override
    public EntityModel<WorkerManagement> process(
        EntityModel<WorkerManagement> model
    ) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/enterworker")
                .withRel("enterworker")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/exitworker")
                .withRel("exitworker")
        );

        return model;
    }
}
