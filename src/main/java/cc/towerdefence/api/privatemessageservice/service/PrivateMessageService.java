package cc.towerdefence.api.privatemessageservice.service;

import cc.towerdefence.api.service.PrivateMessageProto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivateMessageService {
    private final PrivateMessageNotificationService notificationService;

    public PrivateMessageProto.PrivateMessageResponse sendPrivateMessage(PrivateMessageProto.PrivateMessageRequest request) {
        UUID targetId = UUID.fromString(request.getRecipientId());
        UUID senderId = UUID.fromString(request.getSenderId());
        String senderUsername = request.getSenderUsername();
        String message = request.getMessage();

        boolean success = this.notificationService.notifyPrivateMessage(targetId, senderUsername, message);

        return PrivateMessageProto.PrivateMessageResponse.newBuilder()
                .setStatus(success ? PrivateMessageProto.PrivateMessageResponse.Status.OK : PrivateMessageProto.PrivateMessageResponse.Status.PLAYER_NOT_ONLINE)
                .setMessage(message)
                .build();
    }
}
