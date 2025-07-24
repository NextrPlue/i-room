package iroom.service;




import iroom.dto.DashBoardResponse;
import iroom.entity.DashBoard;
import iroom.repository.DashBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final DashBoardRepository dashBoardRepository;
    public DashBoardResponse getDashBoard(String metricType){

        DashBoard dashBoard= dashBoardRepository.findTopByMetricTypeOrderByIdDesc(metricType);

        DashBoardResponse dashBoardDto = new DashBoardResponse(
                dashBoard.getMetricType(),
                dashBoard.getMetricValue(),
                dashBoard.getRecordedAt()
        );


        return dashBoardDto;
    }

}
