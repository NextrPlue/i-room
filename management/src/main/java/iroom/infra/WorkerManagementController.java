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
// @RequestMapping(value="/workerManagements")
@Transactional
public class WorkerManagementController {

    @Autowired
    WorkerManagementRepository workerManagementRepository;

    @RequestMapping(
        value = "/workerManagements/enterworker",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public WorkerManagement enterWorker(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody EnterWorkerCommand enterWorkerCommand
    ) throws Exception {
        System.out.println("##### /workerManagement/enterWorker  called #####");
        WorkerManagement workerManagement = new WorkerManagement();
        workerManagement.enterWorker(enterWorkerCommand);
        workerManagementRepository.save(workerManagement);
        return workerManagement;
    }

    @RequestMapping(
        value = "/workerManagements/exitworker",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public WorkerManagement exitWorker(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody ExitWorkerCommand exitWorkerCommand
    ) throws Exception {
        System.out.println("##### /workerManagement/exitWorker  called #####");
        WorkerManagement workerManagement = new WorkerManagement();
        workerManagement.exitWorker(exitWorkerCommand);
        workerManagementRepository.save(workerManagement);
        return workerManagement;
    }
}
//>>> Clean Arch / Inbound Adaptor
