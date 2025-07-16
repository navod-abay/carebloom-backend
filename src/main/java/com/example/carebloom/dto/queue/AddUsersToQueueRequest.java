package com.example.carebloom.dto.queue;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUsersToQueueRequest {
    private List<QueueUserRequest> users;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueueUserRequest {
        private String name;
        private String email;
    }
}
