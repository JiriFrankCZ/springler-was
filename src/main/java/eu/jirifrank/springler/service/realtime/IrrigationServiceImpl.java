package eu.jirifrank.springler.service.realtime;

import eu.jirifrank.springler.api.action.Action;
import eu.jirifrank.springler.api.action.WateringData;
import eu.jirifrank.springler.api.enums.IOTAction;
import eu.jirifrank.springler.service.communication.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IrrigationServiceImpl implements IrrigationService {

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private RealtimeWeatherService weatherService;

    private Double humidity;

    private Double soilMoisture;

    private Double temperatureInSoil;

    private Double temperatureOutside;


    @Override
    public void doWatering(long duration) {
        communicationService.writeAction(new Action(IOTAction.WATER, new WateringData(duration)));
    }


    @Scheduled(fixedDelay = 15 * 60 * 1000)
    public void reloadMeasurement() {
        communicationService.writeAction(new Action(IOTAction.READ_HUMIDITY, null));
        communicationService.writeAction(new Action(IOTAction.READ_SOIL_MOISTURE, null));
        communicationService.writeAction(new Action(IOTAction.READ_TEMPERATURE, null));
    }

    @Scheduled(fixedDelay = 15 * 60 * 1000)
    public void wateringCheck() {

    }
}
