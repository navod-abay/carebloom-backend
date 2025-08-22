package com.example.carebloom.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorApprovalResponse {
    private PendingVendorResponse vendor;
    private String message;
    private boolean firebaseAccountCreated;
    private boolean shouldSendEmail;

    public VendorApprovalResponse(PendingVendorResponse vendor, String message) {
        this.vendor = vendor;
        this.message = message;
        this.firebaseAccountCreated = vendor.getFirebaseUid() != null && !vendor.getFirebaseUid().isEmpty();
        this.shouldSendEmail = this.firebaseAccountCreated;
    }
}
