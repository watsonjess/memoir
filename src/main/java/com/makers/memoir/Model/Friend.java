package com.makers.memoir.Model;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friendships")
public class Friend {

    @EmbeddedId
    private FriendId id;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @MapsId("requesterId")
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @MapsId("addresseeId")
    @JoinColumn(name = "addressee_id")
    private User addressee;

    public Friend() {}

    public Friend(Long requesterId, Long addresseeId) {
        FriendId friendId = new FriendId();
        friendId.setRequesterId(requesterId);
        friendId.setAddresseeId(addresseeId);
        this.id = friendId;
        this.status = "PENDING";
    }
}
