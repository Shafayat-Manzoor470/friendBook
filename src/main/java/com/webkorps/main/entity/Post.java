package com.webkorps.main.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Post {
	
	@Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	 @ManyToOne
	    @JoinColumn(name = "user_id")
	 @JsonManagedReference 
	    private User user; // User who posted

	    private String imageUrl; // URL or path to the uploaded image
	    private LocalDateTime postTime;

	    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	    @JsonBackReference // Prevent serialization of comments when serializing Post
	    private List<Comment> comments; // List of comments on the post

	    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	    @JsonBackReference        // Prevent serialization of likes when serializing Post
	    private List<Like> likes = new ArrayList<>(); // List of likes on the post

	    
	   
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

		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public LocalDateTime getPostTime() {
			return postTime;
		}

		public void setPostTime(LocalDateTime postTime) {
			this.postTime = postTime;
		}

		public List<Comment> getComments() {
			return comments;
		}

		public void setComments(List<Comment> comments) {
			this.comments = comments;
		}

		public List<Like> getLikes() {
			return likes;
		}

		public void setLikes(List<Like> likes) {
			this.likes = likes;
		}
	    
		
		public int getLikeCount() {
		    return (likes != null) ? likes.size() : 0;
		}
	    
		@Override
		public boolean equals(Object o) {
		    if (this == o) return true;
		    if (!(o instanceof Post)) return false;
		    return id != null && id.equals(((Post) o).getId());
		}

		@Override
		public int hashCode() {
		    return getClass().hashCode();
		}
		
}
