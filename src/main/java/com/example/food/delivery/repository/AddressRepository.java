package com.example.food.delivery.repository;

import com.example.food.delivery.entity.Address;
import com.example.food.delivery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserOrderByIdDesc(User user);
    Optional<Address> findByIdAndUser(Long id, User user);
}
