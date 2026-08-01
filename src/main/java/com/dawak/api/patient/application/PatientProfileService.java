package com.dawak.api.patient.application;

import com.dawak.api.audit.application.AuditService;
import com.dawak.api.common.api.ApiException;
import com.dawak.api.common.web.RequestMetadata;
import com.dawak.api.identity.domain.User;
import com.dawak.api.identity.persistence.UserRepository;
import com.dawak.api.patient.api.dto.ConsentResponse;
import com.dawak.api.patient.api.dto.ProfileCompletionRequest;
import com.dawak.api.patient.api.dto.ProfileResponse;
import com.dawak.api.patient.api.dto.ProfileUpdateRequest;
import com.dawak.api.patient.config.PolicyProperties;
import com.dawak.api.patient.domain.Area;
import com.dawak.api.patient.domain.City;
import com.dawak.api.patient.domain.ConsentRecord;
import com.dawak.api.patient.domain.ConsentTypeV1;
import com.dawak.api.patient.domain.PatientProfile;
import com.dawak.api.patient.persistence.AreaRepository;
import com.dawak.api.patient.persistence.CityRepository;
import com.dawak.api.patient.persistence.ConsentRecordRepository;
import com.dawak.api.patient.persistence.PatientProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class PatientProfileService {
    private final UserRepository users;
    private final PatientProfileRepository profiles;
    private final CityRepository cities;
    private final AreaRepository areas;
    private final ConsentRecordRepository consents;
    private final PolicyProperties policies;
    private final AuditService audit;

    public PatientProfileService(UserRepository users, PatientProfileRepository profiles,
                                 CityRepository cities, AreaRepository areas,
                                 ConsentRecordRepository consents, PolicyProperties policies,
                                 AuditService audit) {
        this.users = users;
        this.profiles = profiles;
        this.cities = cities;
        this.areas = areas;
        this.consents = consents;
        this.policies = policies;
        this.audit = audit;
    }

    @Transactional
    public ProfileResponse complete(UUID userId, ProfileCompletionRequest request, RequestMetadata metadata) {
        if (profiles.existsByUserId(userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "PROFILE_ALREADY_COMPLETED", "The patient profile is already complete.");
        }
        validatePolicyVersions(request.acceptedTermsVersion(), request.acceptedPrivacyVersion());
        validateBirthYear(request.birthYear());
        User user = findUser(userId);
        City city = findCity(request.cityId());
        Area area = findArea(request.areaId(), city.getId());
        Instant now = Instant.now();

        var profile = profiles.save(new PatientProfile(user, clean(request.firstName()), clean(request.lastName()),
                request.birthYear(), city, area));
        user.completeRegistration(normalizeEmail(request.email()), request.preferredLanguage());
        consents.save(new ConsentRecord(user, ConsentTypeV1.TERMS_OF_SERVICE, policies.termsVersion(),
                "PATIENT_REGISTRATION", metadata.ipAddress(), metadata.userAgent(), now));
        consents.save(new ConsentRecord(user, ConsentTypeV1.PRIVACY_POLICY, policies.privacyVersion(),
                "PATIENT_REGISTRATION", metadata.ipAddress(), metadata.userAgent(), now));
        audit.record(user, "PATIENT_REGISTRATION_COMPLETED", "PATIENT_PROFILE", profile.getId(),
                "SUCCESS", metadata, "language=" + request.preferredLanguage());
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(UUID userId) { return toResponse(findProfile(userId)); }

    @Transactional
    public ProfileResponse update(UUID userId, ProfileUpdateRequest request, RequestMetadata metadata) {
        validateBirthYear(request.birthYear());
        PatientProfile profile = findProfile(userId);
        City city = findCity(request.cityId());
        Area area = findArea(request.areaId(), city.getId());
        profile.update(clean(request.firstName()), clean(request.lastName()), request.birthYear(), city, area);
        profile.getUser().updateContact(normalizeEmail(request.email()), request.preferredLanguage());
        audit.record(profile.getUser(), "PATIENT_PROFILE_UPDATED", "PATIENT_PROFILE", profile.getId(),
                "SUCCESS", metadata, null);
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> consents(UUID userId) {
        return consents.findAllByUserIdOrderByGrantedAtDesc(userId).stream()
                .map(record -> new ConsentResponse(record.getId(), record.getConsentType(), record.getDocumentVersion(),
                        record.getStatus(), record.getGrantedAt()))
                .toList();
    }

    private void validatePolicyVersions(String terms, String privacy) {
        if (!policies.termsVersion().equals(terms) || !policies.privacyVersion().equals(privacy)) {
            throw new ApiException(HttpStatus.CONFLICT, "POLICY_VERSION_OUTDATED",
                    "Accept the current Terms and Privacy Policy versions before continuing.");
        }
    }

    private void validateBirthYear(Integer birthYear) {
        if (birthYear != null && birthYear > Year.now().getValue()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_BIRTH_YEAR", "Birth year cannot be in the future.");
        }
    }

    private User findUser(UUID id) {
        return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "ACCOUNT_UNAVAILABLE", "Account unavailable."));
    }

    private PatientProfile findProfile(UUID userId) {
        return profiles.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "PROFILE_INCOMPLETE", "Complete the patient profile first."));
    }

    private City findCity(UUID id) {
        return cities.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CITY", "Select a supported city."));
    }

    private Area findArea(UUID id, UUID cityId) {
        return areas.findByIdAndCityIdAndActiveTrue(id, cityId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AREA", "Select an area in the chosen city."));
    }

    private ProfileResponse toResponse(PatientProfile profile) {
        return new ProfileResponse(profile.getId(), profile.getUser().getPhoneNumber(), profile.getFirstName(),
                profile.getLastName(), profile.getUser().getEmail(), profile.getBirthYear(),
                profile.getUser().getPreferredLanguage(), profile.getCity().getId(), profile.getCity().getNameAr(),
                profile.getCity().getNameEn(), profile.getArea().getId(), profile.getArea().getNameAr(),
                profile.getArea().getNameEn(), profile.getUpdatedAt());
    }

    private String clean(String value) { return value.trim().replaceAll("\\s+", " "); }
    private String normalizeEmail(String email) { return email == null || email.isBlank() ? null : email.trim().toLowerCase(); }
}
