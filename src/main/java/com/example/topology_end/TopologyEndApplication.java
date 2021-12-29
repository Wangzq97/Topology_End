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
    static Configfile c ;


    public static void main(String[] args) {
        switch0 = new telnetClient("VT220","#");
        router0 = new telnetClient("VT220","#");
        router1 = new telnetClient("VT220","#");
        router2 = new telnetClient("VT220","#");
        telnet_controller = new TelnetController();
        c = new Configfile();
        SpringApplication.run(TopologyEndApplication.class, args);
    }


}


