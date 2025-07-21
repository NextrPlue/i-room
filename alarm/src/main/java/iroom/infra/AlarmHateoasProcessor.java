package iroom.infra;

import iroom.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class AlarmHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Alarm>> {

    @Override
    public EntityModel<Alarm> process(EntityModel<Alarm> model) {
        return model;
    }
}
