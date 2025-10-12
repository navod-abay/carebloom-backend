package com.example.carebloom.services.queue;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class QueueSSEService {
    private static final Logger logger = LoggerFactory.getLogger(QueueSSEService.class);
    private final Map<String, List<SseEmitter>> clinicEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String clinicId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        clinicEmitters.computeIfAbsent(clinicId, k -> new ArrayList<>()).add(emitter);
        
        emitter.onCompletion(() -> removeEmitter(clinicId, emitter));
        emitter.onTimeout(() -> removeEmitter(clinicId, emitter));
        emitter.onError(e -> removeEmitter(clinicId, emitter));
        
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Connected to queue updates for clinic: " + clinicId));
        } catch (IOException e) {
            logger.error("Error sending initial SSE message", e);
            removeEmitter(clinicId, emitter);
        }
        
        return emitter;
    }

    public void broadcastQueueUpdate(String clinicId, Object queueData) {
        List<SseEmitter> emitters = clinicEmitters.get(clinicId);
        if (emitters != null) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("queue-update")
                        .data(queueData));
                } catch (IOException e) {
                    logger.error("Error sending SSE message", e);
                    deadEmitters.add(emitter);
                }
            }
            
            // Remove dead emitters
            emitters.removeAll(deadEmitters);
        }
    }

    private void removeEmitter(String clinicId, SseEmitter emitter) {
        List<SseEmitter> emitters = clinicEmitters.get(clinicId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                clinicEmitters.remove(clinicId);
            }
        }
    }
}
