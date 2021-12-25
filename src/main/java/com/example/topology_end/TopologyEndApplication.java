package com.example.topology_end;

import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TopologyEndApplication {
    static telnetClient switch0;
    static telnetClient router0;
    static telnetClient router1;
    static telnetClient router2;
    static TelnetController telnet_controller;

    public static void main(String[] args) {
        switch0 = new telnetClient("Switch0","#");
        router0 = new telnetClient("Router0","#");
        router1 = new telnetClient("Router1","#");
        router2 = new telnetClient("Router2","#");
        telnet_controller = new TelnetController();
        SpringApplication.run(TopologyEndApplication.class, args);
    }


}


