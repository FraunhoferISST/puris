package org.eclipse.tractusx.puris.backend.materialdemand.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
public class DemandSeries {
    @EmbeddedId
    Key key;
    // BPNS
    private String customerLocation;
    // BPNS
    private String expectedSupplierLocation;

    private DemandCategory demandCategory;

    @ElementCollection
    private List<Demand> demands = new ArrayList<>();

    @Embeddable
    public static class Key implements Serializable {
        MaterialDemand.Key foreignKey;
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID uuid;

    }

}
