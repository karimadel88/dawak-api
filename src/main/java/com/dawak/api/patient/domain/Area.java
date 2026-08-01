package com.dawak.api.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "area")
public class Area {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    @Column(nullable = false, length = 50)
    private String code;
    @Column(name = "name_ar", nullable = false, length = 120)
    private String nameAr;
    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;
    @Column(nullable = false)
    private boolean active;

    protected Area() {}
    public UUID getId() { return id; }
    public City getCity() { return city; }
    public String getCode() { return code; }
    public String getNameAr() { return nameAr; }
    public String getNameEn() { return nameEn; }
    public boolean isActive() { return active; }
}
