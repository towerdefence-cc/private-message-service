package cc.towerdefence.api.privatemessageservice.controller;

import cc.towerdefence.api.privatemessageservice.service.PrivateMessageService;
import cc.towerdefence.api.service.PrivateMessageGrpc;
import cc.towerdefence.api.service.PrivateMessageProto;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Controller;

@GrpcService
@Controller
@RequiredArgsConstructor
public class PrivateMessageController extends PrivateMessageGrpc.PrivateMessageImplBase {
    private final PrivateMessageService privateMessageService;

    @Override
    public void sendPrivateMessage(PrivateMessageProto.PrivateMessageRequest request, StreamObserver<PrivateMessageProto.PrivateMessageResponse> responseObserver) {
        responseObserver.onNext(this.privateMessageService.sendPrivateMessage(request));
        responseObserver.onCompleted();
    }
}
