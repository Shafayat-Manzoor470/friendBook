package com.webkorps.main.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Comment {
	
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	 @ManyToOne
	 @JoinColumn(name = "user_id")
	 @JsonIgnoreProperties({"comments", "posts", "hibernateLazyInitializer", "handler"})
	 private User user; // User who commented

	 @ManyToOne
	 @JoinColumn(name = "post_id")
	 @JsonIgnoreProperties({"comments", "likes", "hibernateLazyInitializer", "handler"})
	 private Post post;// Post being commented on

	    private String content; // Comment content
	    private LocalDateTime commentTime;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public User getUser() {
			return user;
		}
		public void setUser(User user) {
			this.user = user;
		}
		public Post getPost() {
			return post;
		}
		public void setPost(Post post) {
			this.post = post;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public LocalDateTime getCommentTime() {
			return commentTime;
		}
		public void setCommentTime(LocalDateTime commentTime) {
			this.commentTime = commentTime;
		}
	    
	    


}
