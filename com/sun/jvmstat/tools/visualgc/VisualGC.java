 package com.sun.jvmstat.tools.visualgc;
 
 import java.io.PrintStream;
 import java.util.Set;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import sun.jvmstat.monitor.MonitorException;
 import sun.jvmstat.monitor.MonitoredHost;
 import sun.jvmstat.monitor.MonitoredVm;
 import sun.jvmstat.monitor.VmIdentifier;
 import sun.jvmstat.monitor.event.HostEvent;
 import sun.jvmstat.monitor.event.HostListener;
 import sun.jvmstat.monitor.event.VmStatusChangeEvent;
 
 public class VisualGC
 {
   private static volatile boolean active = true;
   private static volatile boolean terminated = false;
   private static Arguments arguments;
 
   public static void main(String[] paramArrayOfString)
   {
     try
     {
       arguments = new Arguments(paramArrayOfString);
     }
     catch (IllegalArgumentException localIllegalArgumentException) {
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
 
     String str = arguments.vmIdString();
     int i = arguments.samplingInterval();
 
     MonitoredVmModel localMonitoredVmModel = null;
 
     MonitoredHost localMonitoredHost = null;
     MonitoredVm localMonitoredVm = null;
     try
     {
       VmIdentifier localVmIdentifier = arguments.vmId();
       localMonitoredHost = MonitoredHost.getMonitoredHost(localVmIdentifier);
       localMonitoredVm = localMonitoredHost.getMonitoredVm(localVmIdentifier, i);
 
       localMonitoredVmModel = new MonitoredVmModel(localMonitoredVm);
 
       if (localVmIdentifier.getLocalVmId() != 0) {
         localMonitoredHost.addHostListener(new HostListener(localVmIdentifier.getLocalVmId(), localMonitoredHost)
         {
           VmIdentifier lvmid;
           MonitoredHost host;

	   public void VmId (VmIdentifier lvmid) {
		this.lvmid = lvmid;
	   }
 
           public void vmStatusChanged(VmStatusChangeEvent paramVmStatusChangeEvent)
           {
             if ((paramVmStatusChangeEvent.getTerminated().contains(this.lvmid)) || (!paramVmStatusChangeEvent.getActive().contains(this.lvmid)))
             {
               VisualGC.access$102(true);
             }
           }
 
           public void disconnected(HostEvent paramHostEvent) {
             if (this.host == paramHostEvent.getMonitoredHost()) {
               VisualGC.access$102(true);
             }
 
           }
 
         });
       }
 
     }
     catch (MonitorException localMonitorException)
     {
       if (localMonitorException.getMessage() != null) {
         System.err.println(localMonitorException.getMessage());
       }
       else {
         Throwable localThrowable = localMonitorException.getCause();
         if ((localThrowable != null) && (localThrowable.getMessage() != null)) {
           System.err.println(localThrowable.getMessage());
         }
         else {
           localMonitorException.printStackTrace();
         }
       }
       if ((localMonitoredHost != null) && (localMonitoredVm != null))
         try {
           localMonitoredHost.detach(localMonitoredVm);
         }
         catch (Exception localException1) {
         }
       System.exit(1);
     }
 
     GCSample localGCSample1 = new GCSample(localMonitoredVmModel);
 
     int j = Integer.getInteger("visualheap.x", 0).intValue();
     int k = Integer.getInteger("visualheap.y", 0).intValue();
     int m = Integer.getInteger("visualheap.width", 450).intValue();
     int n = Integer.getInteger("visualheap.height", 600).intValue();
 
     int i1 = Integer.getInteger("graphgc.x", j + m).intValue();
     int i2 = Integer.getInteger("graphgc.y", k).intValue();
     int i3 = Integer.getInteger("graphgc.width", 450).intValue();
     int i4 = Integer.getInteger("graphgc.height", 600).intValue();
 
     int i5 = Integer.getInteger("agetable.x", j).intValue();
     int i6 = Integer.getInteger("agetable.y", k + n).intValue();
     int i7 = Integer.getInteger("agetable.width", i3 + m).intValue();
 
     int i8 = Integer.getInteger("agetable.height", 200).intValue();
 
     GraphGC localGraphGC = new GraphGC(localGCSample1);
     localGraphGC.setBounds(i1, i2, i3, i4);
 
     VisualAgeHistogram localVisualAgeHistogram1 = null;
     if (localGCSample1.ageTableSizes != null) {
       localVisualAgeHistogram1 = new VisualAgeHistogram(localGCSample1);
       localVisualAgeHistogram1.setBounds(i5, i6, i7, i8);
     }
 
     VisualAgeHistogram localVisualAgeHistogram2 = localVisualAgeHistogram1;
     VisualHeap localVisualHeap = new VisualHeap(localGraphGC, localVisualAgeHistogram2, localGCSample1);
     localVisualHeap.setBounds(j, k, m, n);
 
     localVisualHeap.show();
     localGraphGC.show();
     if (localVisualAgeHistogram2 != null) localVisualAgeHistogram2.show();
 
     int i9 = 0;
     GCSample localGCSample2 = null;
 
     while (active)
     {
       try {
         Thread.sleep(i);
       } catch (InterruptedException localInterruptedException) {
       }
       if (terminated) {
         if (i9 != 0)
           continue;
         i9 = 1;
         try
         {
           SwingUtilities.invokeAndWait(new Runnable(localVisualHeap) { private final VisualHeap val$f;
 
             public void run() { String[] arrayOfString = { "Monitored Java Virtual Machine Terminated", " ", "Exit visualgc?", " " };
 
               int i = JOptionPane.showConfirmDialog(this.val$f, arrayOfString, "Target Terminated", 0, 1);
 
               if (i == 0)
                 System.exit(0); } } );
         }
         catch (Exception localException2) {
           localException2.printStackTrace();
         }
         localGCSample2 = new GCSample(localMonitoredVmModel);
         continue;
       }
       GCSample localGCSample3 = localGCSample2 != null ? localGCSample2 : new GCSample(localMonitoredVmModel);
 
       SwingUtilities.invokeLater(new Runnable(localVisualHeap, localGCSample3, localGraphGC, localVisualAgeHistogram2) { private final VisualHeap val$f;
         private final GCSample val$current;
         private final GraphGC val$g;
         private final VisualAgeHistogram val$a;
 
         public void run() { this.val$f.update(this.val$current);
           this.val$g.update(this.val$current);
           if (this.val$a != null) this.val$a.update(this.val$current);
         }
       });
     }
   }
 }

/* Location:           C:\Users\fkessen\jvmstat-3_0\jvmstat\jars\visualgc.jar
 * Qualified Name:     com.sun.jvmstat.tools.visualgc.VisualGC
 * JD-Core Version:    0.6.0
 */
