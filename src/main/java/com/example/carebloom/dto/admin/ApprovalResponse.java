package com.example.carebloom.dto.admin;

import com.example.carebloom.models.MoHOfficeUser;
import lombok.Data;

@Data
public class ApprovalResponse {
    private MoHOfficeUser user;
    private String message;
    private boolean emailSent;
    
    public ApprovalResponse(MoHOfficeUser user, String message, boolean emailSent) {
        this.user = user;
        this.message = message;
        this.emailSent = emailSent;
    }
}
