package eu.jirifrank.springler.api.enums;

public final class ApplicationLocation {

    public static final String BASE_PACKAGE = "eu.jirifrank.springler";
    public static final String ENTITIES = "eu.jirifrank.springler.api.entity";
    public static final String REPOSITORIES = "eu.jirifrank.springler.service.persistence";

    public static final String MQ_QUEUE_MEASUREMENTS = "springler.sensor.measurings";
    public static final String MQ_QUEUE_ACTIONS = "springler.actions";

    private ApplicationLocation() {
    }
}
