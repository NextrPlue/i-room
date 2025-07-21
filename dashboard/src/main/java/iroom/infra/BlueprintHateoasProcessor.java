package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class BlueprintHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Blueprint>> {

    @Override
    public EntityModel<Blueprint> process(EntityModel<Blueprint> model) {
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() +
                    "/registerblueprint"
                )
                .withRel("registerblueprint")
        );
        model.add(
            Link
                .of(model.getRequiredLink("self").getHref() + "/editblueprint")
                .withRel("editblueprint")
        );
        model.add(
            Link
                .of(
                    model.getRequiredLink("self").getHref() + "/deleteblueprint"
                )
                .withRel("deleteblueprint")
        );

        return model;
    }
}
