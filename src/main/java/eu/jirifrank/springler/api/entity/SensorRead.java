package eu.jirifrank.springler.api.entity;

import eu.jirifrank.springler.api.enums.Location;
import eu.jirifrank.springler.api.enums.SensorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_type_location", columnList = "sensorType,location")
})
public class SensorRead {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column
    @Enumerated(EnumType.STRING)
    private SensorType sensorType;

    @Column
    @Enumerated(EnumType.STRING)
    private Location location;

    @Column
    private Double value;

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "sensorReads")
    private List<Irrigation> irrigationList;
}
