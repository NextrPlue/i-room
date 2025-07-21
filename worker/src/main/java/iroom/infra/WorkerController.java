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
// @RequestMapping(value="/workers")
@Transactional
public class WorkerController {

    @Autowired
    WorkerRepository workerRepository;

    @RequestMapping(
        value = "/workers/login",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public Worker login(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody LoginCommand loginCommand
    ) throws Exception {
        System.out.println("##### /worker/login  called #####");
        Worker worker = new Worker();
        worker.login(loginCommand);
        workerRepository.save(worker);
        return worker;
    }

    @RequestMapping(
        value = "/workers/register",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public Worker register(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody RegisterCommand registerCommand
    ) throws Exception {
        System.out.println("##### /worker/register  called #####");
        Worker worker = new Worker();
        worker.register(registerCommand);
        workerRepository.save(worker);
        return worker;
    }

    @RequestMapping(
        value = "/workers/{id}/edit",
        method = RequestMethod.PUT,
        produces = "application/json;charset=UTF-8"
    )
    public Worker edit(
        @PathVariable(value = "id") Long id,
        @RequestBody EditCommand editCommand,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        System.out.println("##### /worker/edit  called #####");
        Optional<Worker> optionalWorker = workerRepository.findById(id);

        optionalWorker.orElseThrow(() -> new Exception("No Entity Found"));
        Worker worker = optionalWorker.get();
        worker.edit(editCommand);

        workerRepository.save(worker);
        return worker;
    }
}
//>>> Clean Arch / Inbound Adaptor
