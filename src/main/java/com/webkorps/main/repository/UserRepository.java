package com.webkorps.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webkorps.main.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByEmail(String email);
    public Optional<User> findByUsername(String username);
    
    // Corrected method names for existence checks
    public boolean existsByEmail(String email);
    public boolean existsByUsername(String username);
    public List<User> findByUsernameContainingIgnoreCase(String query);
	
//	public boolean existsBySenderIdAndReceiverId(long senderId, long receiverId);
}
