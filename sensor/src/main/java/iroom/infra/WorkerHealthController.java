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
// @RequestMapping(value="/workerHealths")
@Transactional
public class WorkerHealthController {

    @Autowired
    WorkerHealthRepository workerHealthRepository;

    @RequestMapping(
        value = "/workerHealths/recordworkerlocation",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public WorkerHealth recordWorkerLocation(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody RecordWorkerLocationCommand recordWorkerLocationCommand
    ) throws Exception {
        System.out.println(
            "##### /workerHealth/recordWorkerLocation  called #####"
        );
        WorkerHealth workerHealth = new WorkerHealth();
        workerHealth.recordWorkerLocation(recordWorkerLocationCommand);
        workerHealthRepository.save(workerHealth);
        return workerHealth;
    }

    @RequestMapping(
        value = "/workerHealths/recordworkerbody",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public WorkerHealth recordWorkerBody(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody RecordWorkerBodyCommand recordWorkerBodyCommand
    ) throws Exception {
        System.out.println(
            "##### /workerHealth/recordWorkerBody  called #####"
        );
        WorkerHealth workerHealth = new WorkerHealth();
        workerHealth.recordWorkerBody(recordWorkerBodyCommand);
        workerHealthRepository.save(workerHealth);
        return workerHealth;
    }
}
//>>> Clean Arch / Inbound Adaptor
