package com.dawak.api.prescription.application;

public interface PrescriptionStorage {
    void write(String key, byte[] content);
    byte[] read(String key);
    void delete(String key);
}
