package com.sun.jvmstat.tools.visualgc;
 
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.management.ManagementFactory;
import java.util.StringTokenizer;

public class NotVisualGCAgent {
  private static volatile boolean halted = false;
  private static volatile boolean active = true;
  private static volatile boolean terminated = false;
  private static String[] newParams;

  public static void premain(String agentArgs) {
    String vmId;
    newParams = new String[6];
    String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
    int p = nameOfRunningVM.indexOf('@');
    int c = 0;
    String pid = nameOfRunningVM.substring(0, p);

    newParams[c++] = pid;
    if (agentArgs != null && agentArgs.length() != 0) {
        StringTokenizer str = new StringTokenizer(agentArgs, ",");
        while(str.hasMoreTokens()) {
            newParams[c++] = str.nextToken();
        }
    } else {
        newParams[c++] = "1000";
    }

    // we are main and there is a finaliser, although we don't need it ....
    final Thread main =  Thread.currentThread();
    final Thread finisher = new Thread("NotVisualGC Finisher") {
        @Override
        public void run() {
            try {
                if (main != null) {
                    NotVisualGCMain x = new NotVisualGCMain(newParams);
                    main.join();
                }
            // main exits...
            } catch (Exception x) {
            // something went wrong with main
                System.err.println(x.getMessage());
            }
        }
    };
// the finisher...
    finisher.setDaemon(true);
    finisher.start();
  }
}
