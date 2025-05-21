package com.webkorps.main.DTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webkorps.main.entity.Comment;
import com.webkorps.main.entity.Post;

public class PostDTO {

	 private Long id;
    private String username;
    private String imageUrl;

    // Use Jackson annotation to serialize date as formatted string in JSON
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;

    private List<CommentDTO> comments;
    private int likeCount;
    private boolean likedByCurrentUser;

    // Constructor using individual fields
    public PostDTO(String username, String imageUrl, Date postTime,
                   List<CommentDTO> comments, int likeCount, boolean likedByCurrentUser) {
        this.username = username;
        this.imageUrl = imageUrl;
        this.postTime = postTime;  // Assign Date directly, not String
        this.comments = comments;
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    // Constructor from Post entity and current user ID
    public PostDTO(Post post, Long currentUserId) {
    	  this.id = post.getId();
        this.username = post.getUser().getUsername();
        this.imageUrl = post.getImageUrl();

        this.postTime = post.getPostTime() == null ? null
            : Date.from(post.getPostTime().atZone(ZoneId.systemDefault()).toInstant());
        this.comments = post.getComments()
                .stream()
                .map(CommentDTO::new)
                .toList();

        this.likeCount = post.getLikes() != null ? post.getLikes().size() : 0;

        this.likedByCurrentUser = post.getLikes() != null &&
                post.getLikes().stream()
                    .anyMatch(like -> currentUserId.equals(like.getUser().getId()));
    }

    // Getters and setters
    // Getter and setter for id
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Date getPostTime() { return postTime; }
    public void setPostTime(Date postTime) { this.postTime = postTime; }

    public List<CommentDTO> getComments() { return comments; }
    public void setComments(List<CommentDTO> comments) { this.comments = comments; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    // Helper method for conversion (optional)
    private Date convertLocalDateTimeToDate(LocalDateTime ldt) {
        if (ldt == null) return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
