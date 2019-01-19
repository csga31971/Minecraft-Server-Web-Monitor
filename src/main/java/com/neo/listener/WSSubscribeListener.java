package com.neo.listener;

import com.neo.config.SystemPropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.io.*;

@SuppressWarnings("ALL")
@Component
public class WSSubscribeListener {

    @Autowired
    private SystemPropertiesConfig systemPropertiesConfig;

    @Autowired
    private SimpMessagingTemplate template;

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event){
        File consoleLog = new File(systemPropertiesConfig.getMcLocation() + "/console.log");
        try {
            FileInputStream inputStream = new FileInputStream(consoleLog);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            char[] chars = new char[inputStream.available()];
            inputStreamReader.read(chars);
            String s = new String(chars);
            template.convertAndSendToUser(event.getUser().getName(),"/ws/console/output", s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
