// Create new file: src/main/java/com/sih/erp/repository/RoomRepository.java
package com.sih.erp.repository;

import com.sih.erp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
}