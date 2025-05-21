package com.webkorps.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webkorps.main.entity.User;
import com.webkorps.main.entity.UserFollow;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    List<UserFollow> findByFollower(User follower);      // Who I follow
    List<UserFollow> findByFollowed(User followed);      // Who follows me

    int countByFollower(User follower);  // Following count
    int countByFollowed(User followed);  // Followers count

    boolean existsByFollowerAndFollowed(User follower, User followed);

    void deleteByFollowerAndFollowed(User follower, User followed);
}
