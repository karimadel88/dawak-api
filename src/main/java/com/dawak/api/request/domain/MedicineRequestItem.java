package com.dawak.api.request.domain;

import com.dawak.api.common.persistence.MutableEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "medicine_request_item")
public class MedicineRequestItem extends MutableEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_request_id", nullable = false, unique = true)
    private MedicineRequest request;
    @Column(name = "medicine_package_id", nullable = false)
    private UUID medicinePackageId;
    @Column(name = "requested_quantity", nullable = false)
    private int requestedQuantity;
    @Column(name = "patient_notes", length = 500)
    private String patientNotes;

    protected MedicineRequestItem() {}
    MedicineRequestItem(MedicineRequest request, UUID medicinePackageId, int quantity, String notes) {
        super(UUID.randomUUID()); this.request = request; this.medicinePackageId = medicinePackageId;
        this.requestedQuantity = quantity; this.patientNotes = notes;
    }
    public UUID getMedicinePackageId() { return medicinePackageId; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public String getPatientNotes() { return patientNotes; }
}
