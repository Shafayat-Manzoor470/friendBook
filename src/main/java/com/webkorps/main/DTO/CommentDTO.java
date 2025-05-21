package com.webkorps.main.DTO;


import java.time.LocalDateTime;

import com.webkorps.main.entity.Comment;

public class CommentDTO {
    private Long id;
    private String content;
    private String username;
    private LocalDateTime commentTime;

    public CommentDTO(Long id, String content, String username, LocalDateTime commentTime) {
        this.id = id;
        this.content = content;
        this.username = username;
        this.commentTime = commentTime;
    }

    
    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent(); 
        this.username = comment.getUser().getUsername(); // Assuming Comment has a User object
        this.commentTime = comment.getCommentTime(); 
    }

    // Getters
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getUsername() { return username; }
    public LocalDateTime getCommentTime() { return commentTime; }
}
