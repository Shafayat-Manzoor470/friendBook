package com.webkorps.main.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webkorps.main.DTO.CommentDTO;
import com.webkorps.main.entity.Comment;
import com.webkorps.main.entity.Post;
import com.webkorps.main.entity.User;
import com.webkorps.main.repository.CommentRepository;

@Service
public class CommentService {
	
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PostService postService;
	
	
	@Autowired
	private CommentRepository commentRepository;
	
	// =============add comments on the post=======================================
	 public Comment addComment(Long postId, String content, String username) {
	        User user = userService.getUserByEmail(username);
	        Post post = postService.getPostById(postId);
	        
	        Comment comment = new Comment();
	        comment.setUser(user);
	        comment.setPost(post);
	        comment.setContent(content);
	        comment.setCommentTime(LocalDateTime.now());
	        
	        return commentRepository.save(comment);
	    }
//===============Get old comments from the post====================================
	 public List<CommentDTO> getCommentsByPost(Long postId) {
		    List<Comment> comments = commentRepository.findByPostIdWithUser(postId);

		    List<CommentDTO> commentDTOs = comments.stream()
		        .map(c -> new CommentDTO(
		                c.getId(),
		                c.getContent(),
		                c.getUser() != null ? c.getUser().getUsername() : "Anonymous",
		                c.getCommentTime()
		        ))
		        .collect(Collectors.toList());

		    commentDTOs.forEach(c -> System.out.println("Comment by user: " + (c.getUsername() != null ? c.getUsername() : "null user")));
		    
		    return commentDTOs;
		}
	

}
