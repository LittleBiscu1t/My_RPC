package Client.rpcClient;

import common.Message.RpcRequest;
import common.Message.RpcResponse;

public interface RpcClient {
    RpcResponse sendRequest(RpcRequest request) throws InterruptedException;
}
