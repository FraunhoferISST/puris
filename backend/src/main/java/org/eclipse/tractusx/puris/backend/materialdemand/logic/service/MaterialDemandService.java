package org.eclipse.tractusx.puris.backend.materialdemand.logic.service;

import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.MaterialDemand;
import org.eclipse.tractusx.puris.backend.materialdemand.domain.repository.MaterialDemandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaterialDemandService {

    @Autowired
    MaterialDemandRepository materialDemandRepository;

    public MaterialDemand create(MaterialDemand materialDemand) {
        materialDemandRepository.findById(materialDemand.getKey());

        return null;
    }

}
