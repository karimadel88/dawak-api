package com.dawak.api.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "city")
public class City {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(name = "name_ar", nullable = false, length = 120)
    private String nameAr;
    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;
    @Column(nullable = false)
    private boolean active;

    protected City() {}
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getNameAr() { return nameAr; }
    public String getNameEn() { return nameEn; }
    public boolean isActive() { return active; }
}
