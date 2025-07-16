package com.techsun.tools.mcp;

import ch.qos.logback.core.util.StatusPrinter;
import org.noear.solon.Solon;

public class DBMcpServerApp {
    public static void main(String[] args) {
        StatusPrinter.setPrintStream(null);
        Solon.start(DBMcpServerApp.class, args);
    }
}
