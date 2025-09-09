package com.web_trade.repository;

import com.web_trade.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
	@Query("SELECT u FROM User u WHERE u.email = ?1")
	public User findByEmail(String email);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.inventory i LEFT JOIN FETCH i.items WHERE u.id = :userId")
	User findUserWithInventoryAndItems(@Param("userId") Long userId);

    User findByUsername(String username);

	List<User> findByUsernameContainingAndIdNot(String username, Long id);

	List<User> findByIdNot(Long id);
}
