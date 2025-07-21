package iroom.infra;

import iroom.config.kafka.KafkaProcessor;
import iroom.domain.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ReadAlarmViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private ReadAlarmRepository readAlarmRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenHealthAnomalPublished_then_CREATE_1(
        @Payload HealthAnomalPublished healthAnomalPublished
    ) {
        try {
            if (!healthAnomalPublished.validate()) return;

            // view 객체 생성
            ReadAlarm readAlarm = new ReadAlarm();
            // view 객체에 이벤트의 Value 를 set 함
            readAlarm.setId(healthAnomalPublished.getId());
            readAlarm.setWorkerId(healthAnomalPublished.getWorkerId());
            readAlarm.setOccuredAt(healthAnomalPublished.getOccuredAt());
            readAlarm.setIncidentType(healthAnomalPublished.getIncidentType());
            readAlarm.setDescription(
                String.valueOf(healthAnomalPublished.getIncidentDescription())
            );
            // view 레파지 토리에 save
            readAlarmRepository.save(readAlarm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
