package com.makers.memoir.model;


import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class FriendId implements Serializable {

    private Long requesterId;
    private Long addresseeId;

}