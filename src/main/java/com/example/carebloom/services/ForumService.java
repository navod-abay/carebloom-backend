package com.example.carebloom.services;

import com.example.carebloom.dto.forum.CreateForumThreadRequest;
import com.example.carebloom.dto.forum.CreateReplyRequest;
import com.example.carebloom.dto.forum.ForumThreadSummaryDTO;
import com.example.carebloom.models.ForumReply;
import com.example.carebloom.models.ForumThread;
import com.example.carebloom.models.Midwife;
import com.example.carebloom.models.MoHOfficeUser;
import com.example.carebloom.models.Mother;
import com.example.carebloom.repositories.ForumThreadRepository;
import com.example.carebloom.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service
public class ForumService {

    @Autowired
    private ForumThreadRepository forumThreadRepository;

    public ForumThread createThread(CreateForumThreadRequest request) {
        
        Mother mother = SecurityUtils.getCurrentMother();
        ForumThread thread = new ForumThread();
        thread.setTitle(request.getTitle());
        thread.setContent(request.getContent());
        thread.setAuthorId(mother.getId());
        thread.setAuthorName(mother.getName());
        thread.setAuthorProfileImage(mother.getProfilePhotoUrl());

        // Determine category
        boolean isMedical;
        if (request.getIsMedical() != null) {
            isMedical = request.getIsMedical();
        } else {
            isMedical = isMedicalQuestion(request.getTitle(), request.getContent());
        }
        thread.setCategory(isMedical ? ForumThread.Category.MEDICAL : ForumThread.Category.NON_MEDICAL);

        return forumThreadRepository.save(thread);
    }

    public ForumThread addReply(String threadId, CreateReplyRequest request) {
        Mother mother = SecurityUtils.getCurrentMother();
        ForumThread thread = forumThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Forum thread not found"));

        // Check if the thread is medical
        if (thread.getCategory() == ForumThread.Category.MEDICAL) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mothers can only reply to non-medical threads.");
        }

        ForumReply reply = new ForumReply();
        reply.setContent(request.getContent());
        reply.setAuthorId(mother.getId());
        reply.setAuthorName(mother.getName());
        reply.setAuthorProfileImage(mother.getProfilePhotoUrl());
        reply.setAuthorRole(ForumReply.AuthorRole.MOTHER);

        if (request.getPath() == null || request.getPath().isEmpty()) {
            // Replying to the main thread
            thread.getReplies().add(reply);
        } else {
            // Replying to a nested reply
            List<ForumReply> replies = thread.getReplies();
            ForumReply parentReply = null;

            for (String replyId : request.getPath()) {
                parentReply = replies.stream()
                        .filter(r -> r.getId().equals(replyId))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent reply not found in path"));
                replies = parentReply.getReplies();
            }

            if (parentReply != null) {
                parentReply.getReplies().add(reply);
            } else {
                // This case should ideally not be reached if path is not empty
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reply path");
            }
        }

        return forumThreadRepository.save(thread);
    }

    public ForumThread addReplyAsMoh(String threadId, CreateReplyRequest request) {
        MoHOfficeUser mohUser = SecurityUtils.getCurrentMohUser();
        ForumThread thread = forumThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Forum thread not found"));

        ForumReply reply = new ForumReply();
        reply.setContent(request.getContent());
        reply.setAuthorId(mohUser.getId());
        reply.setAuthorName(mohUser.getName());
        reply.setAuthorProfileImage(null); // MoH users don't have profile images in this model
        reply.setAuthorRole(ForumReply.AuthorRole.MOH_OFFICE);

        if (request.getPath() == null || request.getPath().isEmpty()) {
            // Replying to the main thread
            thread.getReplies().add(reply);
        } else {
            // Replying to a nested reply
            List<ForumReply> replies = thread.getReplies();
            ForumReply parentReply = null;

            for (String replyId : request.getPath()) {
                parentReply = replies.stream()
                        .filter(r -> r.getId().equals(replyId))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent reply not found in path"));
                replies = parentReply.getReplies();
            }

            if (parentReply != null) {
                parentReply.getReplies().add(reply);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reply path");
            }
        }

        return forumThreadRepository.save(thread);
    }

    public ForumThread addReplyAsMidwife(String threadId, CreateReplyRequest request) {
        Midwife midwife = SecurityUtils.getCurrentMidwife();
        ForumThread thread = forumThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Forum thread not found"));

        ForumReply reply = new ForumReply();
        reply.setContent(request.getContent());
        reply.setAuthorId(midwife.getId());
        reply.setAuthorName(midwife.getName());
        reply.setAuthorProfileImage(null); // Midwives may not have profile images
        reply.setAuthorRole(ForumReply.AuthorRole.MIDWIFE);

        if (request.getPath() == null || request.getPath().isEmpty()) {
            // Replying to the main thread
            thread.getReplies().add(reply);
        } else {
            // Replying to a nested reply
            List<ForumReply> replies = thread.getReplies();
            ForumReply parentReply = null;

            for (String replyId : request.getPath()) {
                parentReply = replies.stream()
                        .filter(r -> r.getId().equals(replyId))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent reply not found in path"));
                replies = parentReply.getReplies();
            }

            if (parentReply != null) {
                parentReply.getReplies().add(reply);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reply path");
            }
        }

        return forumThreadRepository.save(thread);
    }

    public ForumThread getThreadById(String threadId) {
        return forumThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Forum thread not found"));
    }

    public Page<ForumThreadSummaryDTO> getThreads(Pageable pageable) {
        Page<ForumThread> threads = forumThreadRepository.findAll(pageable);
        return threads.map(this::convertToSummaryDTO);
    }

    private ForumThreadSummaryDTO convertToSummaryDTO(ForumThread thread) {
        ForumThreadSummaryDTO dto = new ForumThreadSummaryDTO();
        dto.setId(thread.getId());
        dto.setTitle(thread.getTitle());
        dto.setCategory(thread.getCategory());
        dto.setAuthorId(thread.getAuthorId());
        dto.setAuthorName(thread.getAuthorName());
        dto.setAuthorProfileImage(thread.getAuthorProfileImage());
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setClosed(thread.isClosed());
        dto.setReplyCount(thread.getReplies() != null ? thread.getReplies().size() : 0);
        return dto;
    }

    /**
     * Placeholder for AI model to detect if a question is medical.
     * @param title The title of the thread.
     * @param content The content of the thread.
     * @return True if the question is determined to be medical, false otherwise.
     */
    private boolean isMedicalQuestion(String title, String content) {
        // TODO: Implement AI model for medical question detection
        return true; // Returning true for now as per requirement
    }
}
