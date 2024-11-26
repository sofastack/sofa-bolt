--[[

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

]]
-- ######################################################################################################
--
-- WARN(dunjut):
--
-- This is just an alpha version of wireshark bolt protocol dissector, potential bugs could mislead your
-- troubleshooting to a wrong direction (for example fields may not be correctly parsed in corner cases).
-- 
-- Bug reports and optimizations are welcomed (not a lua expert here...)
--
-- ######################################################################################################
-- Request command protocol for v1
-- ** request definition **
--
--  0     1     2           4           6           8          10           12          14         16
--  +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
--  |proto| type| cmdcode   |ver2 |   requestId           |codec|        timeout        |  classLen |
--  +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
--  |headerLen  | contentLen            |                             ... ...                       |
--  +-----------+-----------+-----------+                                                                                               +
--  |               className + header  + content  bytes                                            |
--  +                                                                                               +
--  |                               ... ...                                                         |
--  +-----------------------------------------------------------------------------------------------+
--
-- 
-- Response command protocol for v1
-- ** response definition **
--
--  0     1     2     3     4           6           8          10           12          14         16
--  +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
--  |proto| type| cmdcode   |ver2 |   requestId           |codec|respstatus |  classLen |headerLen  |
--  +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
--  | contentLen            |                  ... ...                                              |
--  +-----------------------+                                                                       +
--  |                          header  + content  bytes                                             |
--  +                                                                                               +
--  |                               ... ...                                                         |
--  +-----------------------------------------------------------------------------------------------+
--
--
-- ######################################################################################################
-- Request command protocol for v2
-- ** request definition **
-- 
--  0     1     2           4           6           8          10     11     12          14         16
--  +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+------+-----+-----+-----+-----+
--  |proto| ver1|type | cmdcode   |ver2 |   requestId           |codec|switch|   timeout             |
--  +-----------+-----------+-----------+-----------+-----------+------------+-----------+-----------+
--  |classLen   |headerLen  |contentLen             |           ...                                  |
--  +-----------+-----------+-----------+-----------+                                                +
--  |               className + header  + content  bytes                                             |
--  +                                                                                                +
--  |                               ... ...                                  | CRC32(optional)       |
--  +------------------------------------------------------------------------------------------------+
--
--
-- Response command protocol for v2
-- ** response definition **
-- 
--  0     1     2     3     4           6           8          10     11    12          14          16
--  +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+------+-----+-----+-----+-----+
--  |proto| ver1| type| cmdcode   |ver2 |   requestId           |codec|switch|respstatus |  classLen |
--  +-----------+-----------+-----------+-----------+-----------+------------+-----------+-----------+
--  |headerLen  | contentLen            |                      ...                                   |
--  +-----------------------------------+                                                            +
--  |               className + header  + content  bytes                                             |
--  +                                                                                                +
--  |                               ... ...                                  | CRC32(optional)       |
--  +------------------------------------------------------------------------------------------------+
--  respstatus: response status
-- 
-- Author: dunjut
-- Modified by JervyShi


bolt_protocol = Proto("Bolt", "SOFA Bolt Protocol")

-- common fields
proto = ProtoField.int8("bolt.proto", "proto", base.DEC)
typ = ProtoField.int8("bolt.type", "type", base.DEC)
cmdcode = ProtoField.int16("bolt.cmdcode", "cmdcode", base.DEC)
ver2 = ProtoField.int8("bolt.ver2", "ver2", base.DEC) -- not sure what 'ver' actually means
req_id = ProtoField.int32("bolt.req_id", "req_id", base.DEC)
codec = ProtoField.int8("bolt.codec", "codec", base.DEC)

classLen = ProtoField.int16("bolt.class_len", "class_len", base.DEC)
headerLen = ProtoField.int16("bolt.header_len", "header_len", base.DEC)
contentLen = ProtoField.int32("bolt.content_len", "content_len", base.DEC)
bufferLen = ProtoField.int32("bolt.buffer_len", "buffer_len", base.DEC)

-- reqeust only
timeout = ProtoField.int32("bolt.timeout", "timeout", base.DEC)

-- response only
status = ProtoField.int16("bolt.status", "status", base.DEC)
-- for both
classname = ProtoField.string("bolt.classname", "classname", base.ASCII)
payload = ProtoField.none("bolt.payload", "payload", base.HEX)

-- not predefined fields but frequetly used data
trace_id = ProtoField.string("bolt.trace_id", "trace_id", base.ASCII)
rpc_id = ProtoField.string("bolt.rpc_id", "rpc_id", base.ASCII)

-- boltv2 only fields
ver1 = ProtoField.int8("bolt.ver1", "ver1", base.DEC) -- bolt v2 only
switch = ProtoField.int8("bolt.switch", "switch", base.DEC) -- bolt v2 only, crc switch

bolt_protocol.fields = {
    proto,
    ver1, -- boltv2 only 
    typ, cmdcode, ver2, req_id, codec,
    switch, -- boltv2 only
    timeout, -- request only
    status, -- response only
    classLen,
    headerLen,
    contentLen,
    bufferLen,
    classname,
    payload,
    crc, -- boltv2 optional
    trace_id, rpc_id
}

function bolt_protocol.dissector(buffer, pinfo, tree)
    -- Ignore zero-length packets.
    length = buffer:len()
    if length == 0 then
        return
    end

    pinfo.cols.protocol = bolt_protocol.name

    local subtree = tree:add(bolt_protocol, buffer(), "Bolt Protocol Data")
    local headerSubtree = subtree:add(bolt_protocol, buffer(), "Header")
    local payloadSubtree = subtree:add(bolt_protocol, buffer(), "Payload")

    local reader_index = 0

    -- Parse common fields
    local proto_num = buffer(reader_index, 1):uint()
    local proto_name = get_proto_name(proto_num)
    subtree:add(proto, buffer(reader_index, 1)):append_text(" (" .. proto_name .. ")")

    reader_index = reader_index + 1

    if proto_name == "BOLTv1" then
        local type_num = buffer(reader_index, 1):uint()
        local type_name = get_type_name(type_num)
        subtree:add(typ, buffer(reader_index, 1)):append_text(" (" .. type_name .. ")")
        reader_index = reader_index + 1

        local cmdcode_num = buffer(reader_index, 2):uint()
        local cmdcode_name = get_cmdcode_name(cmdcode_num)
        subtree:add(cmdcode, buffer(reader_index, 2)):append_text(" (" .. cmdcode_name .. ")")
        reader_index = reader_index + 2

        subtree:add(ver2, buffer(reader_index, 1))
        reader_index = reader_index + 1
        subtree:add(req_id, buffer(reader_index, 4))
        reader_index = reader_index + 4

        local codec_num = buffer(reader_index, 1):uint()
        local codec_name = get_codec_name(codec_num)
        subtree:add(codec, buffer(reader_index, 1)):append_text(" (" .. codec_name .. ")")
        reader_index = reader_index + 1

        -- for request packets --
        if type_name == "request" or type_name == "oneway" then
            parse_request(buffer, subtree, headerSubtree, payloadSubtree, reader_index)
        end

        -- for response packets --
        if cmdcode_name == "response" then
            parse_response(buffer, subtree, headerSubtree, payloadSubtree, reader_index)
        end
    end

    if proto_name == "BOLTv2" then
        subtree:add(ver1, buffer(reader_index, 1))
        reader_index = reader_index + 1

        local type_num = buffer(reader_index, 1):uint()
        local type_name = get_type_name(type_num)
        subtree:add(typ, buffer(reader_index, 1)):append_text(" (" .. type_name .. ")")
        reader_index = reader_index + 1

        local cmdcode_num = buffer(reader_index, 2):uint()
        local cmdcode_name = get_cmdcode_name(cmdcode_num)
        subtree:add(cmdcode, buffer(reader_index, 2)):append_text(" (" .. cmdcode_name .. ")")
        reader_index = reader_index + 2

        subtree:add(ver2, buffer(reader_index, 1))
        reader_index = reader_index + 1
        subtree:add(req_id, buffer(reader_index, 4))
        reader_index = reader_index + 4

        local codec_num = buffer(reader_index, 1):uint()
        local codec_name = get_codec_name(codec_num)
        subtree:add(codec, buffer(reader_index, 1)):append_text(" (" .. codec_name .. ")")
        reader_index = reader_index + 1

        subtree:add(switch, buffer(reader_index, 1))
        reader_index = reader_index + 1

        -- for request packets --
        if type_name == "request" or type_name == "oneway" then
            parse_request(buffer, subtree, headerSubtree, payloadSubtree, reader_index)
        end

        -- for response packets --
        if type_name == "response" then
            parse_response(buffer, subtree, headerSubtree, payloadSubtree, reader_index)
        end
    end
end

function parse_request(buffer, subtree, headerSubtree, payloadSubtree, reader_index)
    subtree:add(timeout, buffer(reader_index, 4))
    reader_index = reader_index + 4

    local class_len = buffer(reader_index, 2):uint()
    subtree:add(classLen, buffer(reader_index, 2))
    reader_index = reader_index + 2

    -- headers
    local header_len = buffer(reader_index, 2):uint()
    subtree:add(headerLen, buffer(reader_index, 2))
    reader_index = reader_index + 2

    -- payload
    local content_len = buffer(reader_index, 4):uint()
    subtree:add(contentLen, buffer(reader_index, 4))
    reader_index = reader_index + 4

    subtree:add(classname, buffer(reader_index, class_len))
    reader_index = reader_index + class_len

    -- parse header
    parse_headers(buffer, subtree, headerSubtree, reader_index, header_len)
    reader_index = reader_index + header_len

    if buffer:len() >= reader_index + content_len then
        -- parse payload
        payloadSubtree:add(payload, buffer(reader_index, content_len))
        reader_index = reader_index + content_len
    end
end

function parse_response(buffer, subtree, headerSubtree, payloadSubtree, reader_index)
    local status_code = buffer(reader_index, 2):uint()
    local status_name = get_status_name(status_code)
    subtree:add(status, buffer(reader_index, 2)):append_text(" (" .. status_name .. ")")
    reader_index = reader_index + 2

    local class_len = buffer(reader_index, 2):uint()
    subtree:add(classLen, buffer(reader_index, 2))
    reader_index = reader_index + 2

    --  headers
    local header_len = buffer(reader_index, 2):uint()
    subtree:add(headerLen, buffer(reader_index, 2))
    reader_index = reader_index + 2

    -- payload
    local content_len = buffer(reader_index, 4):uint()
    subtree:add(contentLen, buffer(reader_index, 4))
    reader_index = reader_index + 4

    -- parse className
    subtree:add(classname, buffer(reader_index, class_len))
    reader_index = reader_index + class_len

    -- parse headers
    parse_headers(buffer, subtree, headerSubtree, reader_index, header_len)
    reader_index = reader_index + header_len

    if buffer:len() >= reader_index + content_len then
        -- parse payload
        payloadSubtree:add(payload, buffer(reader_index, content_len))
        reader_index = reader_index + content_len
    end
end

-- parse headers from buffer(start, len) and add KV pairs into tree (packet details pane of wireshark)
function parse_headers(buffer, subtree, headerTree, start, len)
    local remain = len
    local index = start
    while remain > 0 do
        local from = index
        local kv_len = 0

        -- header key
        local key_len = buffer(index, 4):uint()
        index = index + 4

        local key_name = buffer(index, key_len):string(ENC_UTF_8)
        index = index + key_len

        -- header value
        local val_len = buffer(index, 4):uint()
        index = index + 4

        kv_len = 4 + key_len + 4 + val_len

        local value = buffer(index, val_len):string(ENC_UTF_8)
        headerTree:add(buffer(from, kv_len), key_name .. ": " .. value)

        -- special cases
        if key_name == "rpc_trace_context.sofaTraceId" then
            subtree:add(trace_id, buffer(index, val_len))
        end

        if key_name == "rpc_trace_context.sofaRpcId" then
            subtree:add(rpc_id, buffer(index, val_len))
        end

        index = index + val_len
        remain = remain - kv_len
    end
end

-- map proto number to proto string.
function get_proto_name(proto)
    local proto_name = "Unknown"

    if proto == 1 then
        proto_name = "BOLTv1"
    elseif proto == 2 then
        proto_name = "BOLTv2"
    elseif proto == 13 then
        proto_name = "TR"
    end

    return proto_name
end

-- map type number to request type string.
function get_type_name(typ)
    local type_name = "Unknown"

    if typ == 0 then
        type_name = "response"
    elseif typ == 1 then
        type_name = "request"
    elseif typ == 2 then
        type_name = "oneway"
    end

    return type_name
end

-- map cmdcode to string representation of command type.
function get_cmdcode_name(cmdcode)
    local cmdcode_name = "Unknown"

    if cmdcode == 0 then
        cmdcode_name = "heartbeat"
    elseif cmdcode == 1 then
        cmdcode_name = "request"
    elseif cmdcode == 2 then
        cmdcode_name = "response"
    end

    return cmdcode_name
end

-- map codec number to codec name.
function get_codec_name(codec)
    local codec_name = "Unknown"

    if codec == 0 then
        codec_name = "hessian"
    elseif codec == 1 then
        codec_name = "hessian2"
    elseif codec == 11 then
        codec_name = "protobuf"
    elseif codec == 12 then
        codec_name = "json"
    end

    return codec_name
end

-- map status code to status string.
function get_status_name(statuscode)
    local status_name = "Unknown"

    if statuscode == 0 then
        status_name = "Success"
    elseif statuscode == 1 then
        status_name = "Error"
    elseif statuscode == 2 then
        status_name = "Server Exception"
    elseif statuscode == 3 then
        status_name = "Unknown"
    elseif statuscode == 4 then
        status_name = "Server Thread Pool Busy"
    elseif statuscode == 5 then
        status_name = "Error Comm" -- not sure what exactly this means...
    elseif statuscode == 6 then
        status_name = "No Processor"
    elseif statuscode == 7 then
        status_name = "Timeout"
    elseif statuscode == 8 then
        status_name = "Client Send Error"
    elseif statuscode == 9 then
        status_name = "Codec Exception"
    elseif statuscode == 16 then
        status_name = "Connection Closed" -- 16 is from 0x10 ... (previous codes are 0x00..0x09 -.-!)
    elseif statuscode == 17 then
        status_name = "Server Serial Exception"
    elseif statuscode == 18 then
        status_name = "Server Deserial Exception"
    end

    return status_name
end

-- register our dissector upon tcp port 12200 (default)
local ports = {12200}
-- 如果你想此 bolt 协议的解析脚本去解析更多的端口那么可以在此处添加更多的端口
-- If you want the bolt protocol parsing script to parse more ports then you can add more ports here
-- like: local ports = {12200, 12199, 12198}

for _, port in ipairs(ports) do
    DissectorTable.get("tcp.port"):add(port, bolt_protocol)
end