package com.webkorps.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkorps.main.entity.FriendRequest;
import com.webkorps.main.entity.User;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    FriendRequest findByFromUserAndToUser(User from, User to);
    List<FriendRequest> findByToUserAndStatus(Optional<User> user, String status);
	boolean existsByFromUserAndToUser(User fromUser, User toUser);
}
