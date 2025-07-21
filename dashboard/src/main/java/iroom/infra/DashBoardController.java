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
// @RequestMapping(value="/dashBoards")
@Transactional
public class DashBoardController {

    @Autowired
    DashBoardRepository dashBoardRepository;

    @RequestMapping(
        value = "/dashBoards/exportreport",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public DashBoard exportReport(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody ExportReportCommand exportReportCommand
    ) throws Exception {
        System.out.println("##### /dashBoard/exportReport  called #####");
        DashBoard dashBoard = new DashBoard();
        dashBoard.exportReport(exportReportCommand);
        dashBoardRepository.save(dashBoard);
        return dashBoard;
    }

    @RequestMapping(
        value = "/dashBoards/createimprovement",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8"
    )
    public DashBoard createImprovement(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestBody CreateImprovementCommand createImprovementCommand
    ) throws Exception {
        System.out.println("##### /dashBoard/createImprovement  called #####");
        DashBoard dashBoard = new DashBoard();
        dashBoard.createImprovement(createImprovementCommand);
        dashBoardRepository.save(dashBoard);
        return dashBoard;
    }
}
//>>> Clean Arch / Inbound Adaptor
