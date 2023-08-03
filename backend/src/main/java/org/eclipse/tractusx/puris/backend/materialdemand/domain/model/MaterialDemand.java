package org.eclipse.tractusx.puris.backend.materialdemand.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MaterialDemand {

    @EmbeddedId
    private Key key;

    @OneToMany(cascade = CascadeType.ALL)
    private List<DemandSeries> demandSeries = new ArrayList<>();
    // BPNL
    private String supplier;
    // example "Spark Plug"
    private String materialDescriptionCustomer;

    private UUID materialNumberCatenaX;

    private String materialNumberCustomer;

    private String materialNumberSupplier;

    private UnitOfMeasure unitOfMeasure;

    private LocalDateTime changedAt;



    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private UUID materialDemandId;
        // BPNL
        private String customer;

    }


}
