package com.webkorps.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webkorps.main.entity.Post;
import com.webkorps.main.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIn(List<User> users); 

	List<Post> findByUserInOrderByPostTimeDesc(List<User> followings);

	List<Post> findByUserOrderByPostTimeDesc(User user);

	List<Post> findByUserId(long id);

	List<Post> findByUser(User user);

	@Query("SELECT DISTINCT p FROM Post p WHERE p.user IN :users ORDER BY p.postTime DESC")
	 @EntityGraph(attributePaths = {"comments", "likes", "user"})
	    List<Post> findPostsWithCommentsAndLikesByUsers(@Param("users") List<User> users);

//	    @EntityGraph(attributePaths = {"comments", "likes", "user"})
//	    @Query("SELECT p FROM Post p WHERE p.user = :user ORDER BY p.postTime DESC")
//	    List<Post> findPostsWithCommentsAndLikesByUser(@Param("user") User user);
//	    
//	    @EntityGraph(attributePaths = {"comments", "likes", "user"})
//	    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds ORDER BY p.postTime DESC")
//	    List<Post> findPostsWithCommentsAndLikesByUserIds(@Param("userId") List<Long> userIds);
//	List<Post> getPostsByUser(long id);
}
