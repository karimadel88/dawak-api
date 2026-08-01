package com.dawak.api.patient.domain;

import com.dawak.api.common.persistence.MutableEntity;
import com.dawak.api.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "patient_profile")
public class PatientProfile extends MutableEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(name = "birth_year")
    private Integer birthYear;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    protected PatientProfile() {}

    public PatientProfile(User user, String firstName, String lastName, Integer birthYear, City city, Area area) {
        super(UUID.randomUUID());
        this.user = user;
        update(firstName, lastName, birthYear, city, area);
    }

    public void update(String firstName, String lastName, Integer birthYear, City city, Area area) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
        this.city = city;
        this.area = area;
    }

    public User getUser() { return user; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Integer getBirthYear() { return birthYear; }
    public City getCity() { return city; }
    public Area getArea() { return area; }
}
