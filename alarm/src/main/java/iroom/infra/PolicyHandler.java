package iroom.infra;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import iroom.config.kafka.KafkaProcessor;
import iroom.domain.*;
import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    AlarmRepository alarmRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='AlertedIdle'"
    )
    public void wheneverAlertedIdle_유휴근로자발생(
        @Payload AlertedIdle alertedIdle
    ) {
        AlertedIdle event = alertedIdle;
        System.out.println(
            "\n\n##### listener 유휴근로자발생 : " + alertedIdle + "\n\n"
        );

        // Sample Logic //
        Alarm.유휴근로자발생(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
