// Create new file: src/main/java/com/sih/erp/service/RoomService.java
package com.sih.erp.service;

import com.sih.erp.entity.Room;
import com.sih.erp.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room save(Room room) {
        // You can add validation logic here later
        return roomRepository.save(room);
    }
}