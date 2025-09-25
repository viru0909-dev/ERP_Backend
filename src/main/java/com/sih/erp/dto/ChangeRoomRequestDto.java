// Create new file: src/main/java/com/sih/erp/dto/ChangeRoomRequestDto.java
package com.sih.erp.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ChangeRoomRequestDto {
    private UUID newRoomId;
}