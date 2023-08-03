package org.eclipse.tractusx.puris.backend.materialdemand.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Embeddable
public class Demand {

    private LocalDateTime pointInTime;

    private DemandRate demandRate;

    private boolean fixedPointQuantityFlag;

    double fixedPointConstraint;

    double rangeLowerBoundary;
    double rangeUpperBoundary;

    @Override
    public String toString() {
        String output = "Demand{" +
            "pointInTime=" + pointInTime +
            ", demandRate=" + demandRate;
        if (fixedPointQuantityFlag) {
            output += "\", fixedPointConstraint=\" + fixedPointConstraint";
        } else {
            output += ", rangeLowerBoundary=" + rangeLowerBoundary +
                ", rangeUpperBoundary=" + rangeUpperBoundary;
        }
        return output +  '}';
    }
}
