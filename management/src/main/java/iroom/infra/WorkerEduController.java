package iroom.infra;

import iroom.domain.*;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/workerEdus")
@Transactional
public class WorkerEduController {

    @Autowired
    WorkerEduRepository workerEduRepository;

    @RequestMapping(
        value = "/workerEdus/recordedu",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public WorkerEdu recordEdu(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody RecordEduCommand recordEduCommand
    ) throws Exception {
        System.out.println("##### /workerEdu/recordEdu  called #####");
        WorkerEdu workerEdu = new WorkerEdu();
        workerEdu.recordEdu(recordEduCommand);
        workerEduRepository.save(workerEdu);
        return workerEdu;
    }
}
//>>> Clean Arch / Inbound Adaptor
