# Bolt wireshark plugin

How to use it:

* For Mac or Linux:
```
$ sh install.sh
```
* For Windows:
```
$ .\install.bat
```

Then open wireshark and decode package as BOLT.

Bolt package will be decoded like this:

```
Bolt Protocol Data
    Header
        rpc_trace_context.sofaRpcId: 0
        rpc_trace_context.sofaTraceId: 0bxxxx335162832343267634611586
        rpc_trace_context.sofaCallerIp: 1.2.3.4
        service: com.sofastack.demo.Service:1.0
        rpc_trace_context.sofaCallerApp: test-app
        sofa_head_method_name: hello
    Payload
        payload
    proto: 2 (BOLTv2)
    ver1: 1
    type: 1 (request)
    cmdcode: 1 (request)
    ver2: 1
    req_id: 0
    codec: 11 (protobuf)
    switch: 1
    timeout: 3000
    class_len: 44
    header_len: 691
    content_len: 65
    classname: com.alipay.sofa.rpc.core.request.SofaRequest
    rpc_id: 0
    trace_id: 0bxxxx335162832343267634611586
```

For advanced usage, you can search for any property under the bolt protocol, such as:
```
bolt.trace_id == 0bxxxx335162832343267634611586 
```

如果你想此 bolt 协议的解析脚本去解析更多的端口那么可以在此处添加更多的端口

If you want the bolt protocol parsing script to parse more ports then you can add more ports here

example:

```lua
local ports = {12200, 12199, 12198}

for _, port in ipairs(ports) do
    DissectorTable.get("tcp.port"):add(port, bolt_protocol)
end
```
