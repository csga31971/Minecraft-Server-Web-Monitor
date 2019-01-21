package xyz.csga.listener;

import xyz.csga.config.SystemPropertiesConfig;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

@WebListener
public class StartListener implements ServletContextListener {

    @Autowired
    SimpMessagingTemplate template;

    @Autowired
    private SystemPropertiesConfig systemPropertiesConfig;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {


        new Thread(() -> {
            Timer statusTimer = new Timer();
            TimerTask statusTask = new TimerTask() {
                @Override
                public void run() {
                    String statusData = "{\"totalMemory\": ";
                    OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    statusData += (os.getTotalPhysicalMemorySize() / 1048576);
                    statusData += ", \"freeMemory\": ";
                    statusData += os.getFreePhysicalMemorySize() / 1048576;
                    statusData += ", \"cpuUsage\": ";
                    statusData += os.getSystemCpuLoad();
                    statusData += "}";
                    template.convertAndSend("/ws/system", statusData);
                }
            };
            statusTimer.schedule(statusTask, 0, 5000);
            statusTask.run();
        }).start();

        Path path = Paths.get(systemPropertiesConfig.getMcLocation());
//        Path path = Paths.get("D:\\temp");
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            path.register(watcher, ENTRY_MODIFY);
            new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watcher.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == OVERFLOW) {
                                //事件可能lost or discarded
                                continue;
                            } else if (event.kind() == ENTRY_MODIFY) {
                                Path fileName = (Path) event.context();
                                if (fileName.toString().equals("console.log")) {
                                    File consoleLog = new File(systemPropertiesConfig.getMcLocation() + "/console.log");
//                                if (fileName.toString().equals("server.properties")) {
//                                    File consoleLog = new File("D:\\temp\\server.properties");
                                    try {
                                        FileInputStream inputStream = new FileInputStream(consoleLog);
                                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                        char[] chars = new char[inputStream.available()];
                                        inputStreamReader.read(chars);
                                        String s = new String(chars);
                                        template.convertAndSend("/ws/console/output", s);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (!key.reset()) { // 重设WatchKey
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

    }

}
