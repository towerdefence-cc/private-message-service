package cc.towerdefence.api.privatemessageservice.service;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.api.service.velocity.VelocityPrivateMessageGrpc;
import cc.towerdefence.api.service.velocity.VelocityPrivateMessageProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivateMessageNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateMessageNotificationService.class);

    private final PlayerTrackerGrpc.PlayerTrackerBlockingStub playerTracker;
    private final CoreV1Api kubernetesClient;

    @Async
    public boolean notifyPrivateMessage(UUID recipientId, String senderUsername, String message) {
        Optional<String> optionalTargetServerIp = this.getServerIpForPlayer(recipientId);
        if (optionalTargetServerIp.isEmpty()) return false;
        String targetServerIp = optionalTargetServerIp.get();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(targetServerIp, 9090)
                .usePlaintext()
                .build();

        VelocityPrivateMessageGrpc.VelocityPrivateMessageBlockingStub stub = VelocityPrivateMessageGrpc.newBlockingStub(channel);

        stub.receiveMessage(VelocityPrivateMessageProto.PrivateMessage.newBuilder()
                .setRecipientId(recipientId.toString())
                .setSenderUsername(senderUsername)
                .setMessage(message).buildPartial());

        return true;
    }

    public Optional<String> getServerIpForPlayer(UUID playerId) {
        PlayerTrackerProto.GetPlayerServerResponse response = this.playerTracker.getPlayerServer(PlayerTrackerProto.GetPlayerServerRequest.newBuilder()
                .setPlayerId(playerId.toString())
                .build());

        if (!response.hasServer()) return Optional.empty();

        String proxyId = response.getServer().getProxyId();

        try {
            V1Pod pod = this.kubernetesClient.readNamespacedPod(proxyId, "towerdefence", null);
            return Optional.ofNullable(pod.getStatus().getPodIP());
        } catch (ApiException e) {
            LOGGER.error("Failed to get pod for proxy id {}:\nK8s Error: ({}) {}\n{}", proxyId, e.getCode(), e.getResponseBody(), e);
            return Optional.empty();
        }
    }
}
