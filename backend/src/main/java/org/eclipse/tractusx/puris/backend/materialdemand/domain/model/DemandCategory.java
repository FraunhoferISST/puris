package org.eclipse.tractusx.puris.backend.materialdemand.domain.model;

public enum DemandCategory {

    SmallSeries("OI01"),
    PhaseOutPeriod(" PO01"),
    AfterSales("A1S1"),
    ExtraordinaryDemand("ED01"),
    PhaseInPeriod("PI01"),
    Series("SR99"),
    Default("0001"),
    SingleOrder("OS01");


    private DemandCategory(String code) {
        CODE = code;
    }
    final String CODE;

}
