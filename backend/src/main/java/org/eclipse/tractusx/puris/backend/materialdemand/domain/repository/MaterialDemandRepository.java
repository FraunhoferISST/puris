package org.eclipse.tractusx.puris.backend.materialdemand.domain.repository;

import org.eclipse.tractusx.puris.backend.materialdemand.domain.model.MaterialDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialDemandRepository extends JpaRepository<MaterialDemand, MaterialDemand.Key> {

}
