package com.techsun.tools.mcp;

import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;

@McpServerEndpoint(channel = McpChannel.STDIO)
public class PostgresMcpTool {
    @ToolMapping(description = "查询天气预报")
    public String get_weather(@Param(name = "location", description = "城市位置") String location) {
        return "晴，14度";
    }
}

