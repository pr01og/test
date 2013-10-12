package com.probojnik.terminal.network;

/**
 * @author Stanislav Shamji
 */
public enum RequestParam {
    RequestType("request_type"),
    Department("departament"),
    Parent("parent"),
    GroupID("groupid"),
    OvirServiceID("ovir_serviceid"),
    URL("url"),
    TerminalID("terminalid");

    private final String param;

    private RequestParam(String param) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }

    @Override
    public String toString() {
        return param;
    }
}
