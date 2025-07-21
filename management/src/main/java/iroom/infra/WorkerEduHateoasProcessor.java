package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class WorkerEduHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<WorkerEdu>> {

    @Override
    public EntityModel<WorkerEdu> process(EntityModel<WorkerEdu> model) {
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/recordedu")
                .withRel("recordedu")
        );

        return model;
    }
}
