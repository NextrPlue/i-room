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
public class ReadWorkerViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private ReadWorkerRepository readWorkerRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRoleModified_then_CREATE_1(
        @Payload RoleModified roleModified
    ) {
        try {
            if (!roleModified.validate()) return;

            // view 객체 생성
            ReadWorker readWorker = new ReadWorker();
            // view 객체에 이벤트의 Value 를 set 함
            // view 레파지 토리에 save
            readWorkerRepository.save(readWorker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
