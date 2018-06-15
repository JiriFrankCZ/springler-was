package eu.jirifrank.springler.service.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.jirifrank.springler.api.action.Action;
import eu.jirifrank.springler.api.entity.SensorRead;
import eu.jirifrank.springler.api.enums.ApplicationLocation;
import eu.jirifrank.springler.api.request.LogRequest;
import eu.jirifrank.springler.api.request.SensorReadRequest;
import eu.jirifrank.springler.api.request.SensorReadRequestList;
import eu.jirifrank.springler.service.logging.LoggingService;
import eu.jirifrank.springler.service.persistence.SensorReadRepository;
import eu.jirifrank.springler.util.NumberUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class RealtimeCommunicationService implements CommunicationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SensorReadRepository sensorReadRepository;

    @Autowired
    private LoggingService loggingService;

    @RabbitListener(queues = {ApplicationLocation.MQ_QUEUE_MEASUREMENTS})
    public void recieveSensorMessage(Message message) {
        log.debug(message.toString());

        Optional.ofNullable(deserializeFromByteArray(message.getBody(), SensorReadRequestList.class))
                .or(() -> {
                    SensorReadRequest sensorReadRequest = deserializeFromByteArray(message.getBody(), SensorReadRequest.class);
                    if (sensorReadRequest != null) {
                        return Optional.of(new SensorReadRequestList(Arrays.asList(sensorReadRequest)));
                    } else {
                        return Optional.empty();
                    }
                }).ifPresent(sensorReadRequestList -> sensorReadRequestList.getSensorReadRequestList()
                .parallelStream()
                .forEach(sensorReadRequest -> {
                    if (sensorReadRequest == null || !validator.validate(sensorReadRequest).isEmpty()) {
                        log.warn("Message thrown away.", message);
                        return;
                    }

                    SensorRead sensorRead = SensorRead.builder()
                            .serviceType(sensorReadRequest.getServiceType())
                            .sensorType(sensorReadRequest.getSensorType())
                            .created(sensorReadRequest.getCreated() == null ? new Date() : sensorReadRequest.getCreated())
                            .location(sensorReadRequest.getLocation())
                            .value(NumberUtils.roundToHalf(sensorReadRequest.getValue()))
                            .build();

                    sensorReadRepository.save(sensorRead);
                    loggingService.log("Sensor read["
                                    + sensorReadRequest.getSensorType() + " ,"
                                    + sensorReadRequest.getLocation() + ", "
                                    + sensorReadRequest.getValue() + "] was saved.",
                            sensorReadRequest.getServiceType()
                    );
                }));
    }

    @RabbitListener(queues = {ApplicationLocation.MQ_QUEUE_LOGS})
    public void recieveLogMessage(Message message) {
        log.debug(message.toString());

        LogRequest logRequest = deserializeFromByteArray(message.getBody(), LogRequest.class);

        if (logRequest == null || !validator.validate(logRequest).isEmpty()) {
            log.warn("Message thrown away.", message);
            return;
        }

        loggingService.log(
                logRequest.getMessage(),
                logRequest.getServiceType()
        );
    }

    @Override
    public void sendActionMessage(Action action) {
        byte[] serializedAction = serializeToByteArray(action);
        rabbitTemplate.convertAndSend(ApplicationLocation.MQ_QUEUE_DEFAULT_EXCHANGE, ApplicationLocation.MQ_QUEUE_ACTIONS, serializedAction);
    }

    private byte[] serializeToByteArray(Action action) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(action);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Not valid object passed for serialization to byte array.", e);
        }
    }

    private <T> T deserializeFromByteArray(byte[] source, Class<T> toClass) {
        try {
            return OBJECT_MAPPER.readValue(source, toClass);
        } catch (IOException e) {
            log.error("Deserialization error.", e);
        }

        return null;
    }
}
