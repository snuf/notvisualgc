package com.sun.jvmstat.tools.visualgc;
 
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.HostListener;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

public class NotVisualGCMain {
   private static volatile boolean halted = false;
   private static volatile boolean active = true;
   private static volatile boolean terminated = false;
   private static Arguments arguments;
   private static String[] Params;
   private static ReSocket gsocket;

   public NotVisualGCMain(String[] Params) {
     try {
        for (String par : Params) {
            System.err.println("Params:" + par);
        }
       arguments = new Arguments(Params);
     } catch (IllegalArgumentException localIllegalArgumentException) {
       System.err.println(localIllegalArgumentException.getMessage());
       Arguments.printUsage(System.err);
       System.exit(1);
     }
 
     if (arguments.isHelp()) {
       Arguments.printUsage(System.out);
       System.exit(1);
     }
 
     if (arguments.isVersion()) {
       Arguments.printVersion(System.out);
       System.exit(1);
     }

     // Dirty but effective....., get variables
     final int lvmid = new Integer(arguments.vmIdString()).intValue(); 
     int i = arguments.samplingInterval();
     String FileName = arguments.FileName();
     File Fh = new File(FileName);
     String GraphiteHost = arguments.GraphiteHost();
     String GraphitePrepend = arguments.GraphitePrepend();
     int GraphitePort = new Integer(arguments.GraphitePort());
    // = new Integer(arguments.GraphitePort()).intValue();
     String GraphiteProto = arguments.GraphiteProto();
     if (GraphiteHost != null) {
        if (GraphitePort < 1) {
            GraphitePort = 2003;
        }
        gsocket = new ReSocket(GraphiteHost, GraphitePort);
     }

     MonitoredVmModel localMonitoredVmModel = null;
     MonitoredHost localMonitoredHost = null;
     MonitoredVm localMonitoredVm = null;

     // setup main monitor, or try to
     try {
       VmIdentifier localVmIdentifier = arguments.vmId();
       localMonitoredHost = MonitoredHost.getMonitoredHost(localVmIdentifier);
       localMonitoredVm = localMonitoredHost.getMonitoredVm(localVmIdentifier, i);
 
       // how are we going to wait here for the main thread to spawn... I don't think we can..
       localMonitoredVmModel = new MonitoredVmModel(localMonitoredVm);

       // we need to get the VmId in here, to make sure we can see if the damn thing was terminated	
       if (localVmIdentifier.getLocalVmId() != 0) {
            localMonitoredHost.addHostListener (
                new HostListener() {
                    MonitoredHost host;
                        public void vmStatusChanged(VmStatusChangeEvent paramVmStatusChangeEvent) {
                            if ((paramVmStatusChangeEvent.getTerminated().contains(lvmid)) ||
                                    (!paramVmStatusChangeEvent.getActive().contains(lvmid))) {
                                terminated = true;
                            } 
                        }

                        public void disconnected(HostEvent paramHostEvent) {
                        if (this.host == paramHostEvent.getMonitoredHost()) {
                            terminated = true;
                        }
                    }
                }
            );
       } else {
            System.err.println("No Vmid given");
       }
     } catch (MonitorException localMonitorException) {
       if (localMonitorException.getMessage() != null) {
         System.err.println("Monitor exception: "+ localMonitorException.getMessage());
       } else {
         Throwable localThrowable = localMonitorException.getCause();
         if ((localThrowable != null) && (localThrowable.getMessage() != null)) {
           System.err.println("Throwable "+ localThrowable.getMessage());
         } else {
           localMonitorException.printStackTrace();
         }
       }
       if ((localMonitoredHost != null) && (localMonitoredVm != null))
         try {
           localMonitoredHost.detach(localMonitoredVm);
         } catch (Exception localException1) {
            localException1.printStackTrace();
         }
         System.exit(1);
     }

     GCSample localGCSample1 = new GCSample(localMonitoredVmModel);
     int i9 = 0;
     GCSample localGCSample2 = null;
     GCSample previousSample = null;

     // we move into our loopedyloopy 
     while (active) {
       try {
         Thread.sleep(i);
       } catch (InterruptedException localInterruptedException) {
            localInterruptedException.printStackTrace();
       }

       // this looks mighty useless while looking at it....
       if (terminated) {
         if (i9 != 0) {
           continue;
         }
         i9 = 1;
         try {
            System.exit(1); 
         } catch (Exception localException2) {
           localException2.printStackTrace();
         }
         localGCSample2 = new GCSample(localMonitoredVmModel);
         continue;
       }

       // check the samples
       GCSample localGCSample3 = localGCSample2 != null ? localGCSample2 : new GCSample(localMonitoredVmModel);
       if (previousSample == null) {
             // we received no data, so we're halted 
             previousSample = localGCSample3;
             halted = true;
       } else {
             halted = false;
       }

       // output, and save last sample as previous afterwards ... erh... shit....
       PrintGC Pgc = new PrintGC(localGCSample3, previousSample);
       /* Graphite bits, should have a seperate lib/thread connecting */
       if (GraphiteHost != null) {
            long epoch = System.currentTimeMillis()/1000;
            /* must do the connect stub earlier... how ? */
            Map<String, Long> map = Pgc.ReturnMap();
            /* traverse through the map */
            for (Map.Entry<String, Long> entry : map.entrySet()) { 
                String data = GraphitePrepend + "." + entry.getKey() +
                    " " + entry.getValue() + " " + epoch;
                /* System.out.println(data); */
                /* construct the line to send in the send bitsy */
                gsocket.write(data);
            }
       /* local file bits */
       } else if (Fh.FileName() != null) {
            if (halted  == true) {
                Fh.AppendToFile(Fh.FileName() + "-" + Fh.Stamp(), Pgc.ReturnHeader());
            }
            Fh.AppendToFile(Fh.FileName() + "-" + Fh.Stamp(), Pgc.ReturnLine());
       /* simple text output locally */
       } else {
           Pgc.printTextComponents();
       }
       previousSample = localGCSample3;
     }
     // we terminated, active was set to false.
   }
}
