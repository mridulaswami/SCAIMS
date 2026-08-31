package com.schoolerp.usermanagement.modules.inspection.repository;

import com.schoolerp.usermanagement.modules.inspection.entity.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface InspectionRepository extends JpaRepository<InspectionEntity, UUID>{

}
