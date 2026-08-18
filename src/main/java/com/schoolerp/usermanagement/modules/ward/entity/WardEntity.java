package com.schoolerp.usermanagement.modules.ward.entity;

import com.schoolerp.usermanagement.modules.WorkOrder.entity.WorkOrderEntity;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.util.UUID;

@Table(name="ward")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;


    @Column(name="name")
    private String name;

    @Column(name="boundary" , columnDefinition = "geometry(Polygon,4326)")
    private Geometry boundary;

    @Column(name="population")
    private int population;



}
