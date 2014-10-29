 package com.sun.jvmstat.tools.visualgc;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import sun.jvmstat.monitor.VmIdentifier;
 
 public class Arguments
 {
   private static boolean debug = Boolean.getBoolean("Arguments.debug");
   private static final int DEFAULT_INTERVAL = 500;
   private static final String VERSION_FILE = "version";
   private boolean help;
   private boolean version;
   private int interval = -1;
   private String vmIdString;
   private VmIdentifier vmId;
   private String FileName;
   private String GraphitePrepend;
   private String GraphiteHost;
   private String GraphitePort = "2003";
   private String GraphiteProto = "tcp";
 
   public static void printUsage(PrintStream paramPrintStream)
   {
     printVersion(paramPrintStream);
     paramPrintStream.println("usage: (not)visualgc -help");
     paramPrintStream.println("       (not)visualgc <vmid> [<interval>]");
     paramPrintStream.println("       notvisualgc <vmid> [<interval>] [<filename>]");
     paramPrintStream.println("       notvisualgc <vmid> [<interval>] [<prepend>] [<host>]");
     paramPrintStream.println("       notvisualgc <vmid> [<interval>] [<prepend>] [<host>] [<port>] [<protoco>]");
     paramPrintStream.println();
     paramPrintStream.println("Definitions:");
     paramPrintStream.println("  <vmid>        Virtual Machine Identifier. A vmid takes the following form:");
     paramPrintStream.println("                     <lvmid>[@<hostname>[:<port>]]");
     paramPrintStream.println("                Where <lvmid> is the local vm identifier for the target");
     paramPrintStream.println("                Java virtual machine, typically a process id; <hostname> is");
     paramPrintStream.println("                the name of the host running the target Java virtual machine;");
     paramPrintStream.println("                and <port> is the port number for the rmiregistry on the");
     paramPrintStream.println("                target host. See the visualgc documentation for a more complete");
     paramPrintStream.println("                description of a <vmid>.");
     paramPrintStream.println("  <interval>    Sampling interval. The following forms are allowed:");
     paramPrintStream.println("                    <n>[\"ms\"|\"s\"]");
     paramPrintStream.println("                Where <n> is an integer and the suffix specifies the units as ");
     paramPrintStream.println("                milliseconds(\"ms\") or seconds(\"s\"). The default units are \"ms\".");
     paramPrintStream.println("                The default interval is 500ms");
     paramPrintStream.println("  <filename>    Write output to a file (notvisualgc only)");
     paramPrintStream.println("  <prepend>     Prepended dot seperated list used in front of the counters sent to");
     paramPrintStream.println("                Graphite \"java.notvisualgc.myfunkyapp.instance.1\" will be sent as");
     paramPrintStream.println("                \"java.notvisualgc.myfunkyapp.instance.1.survivor.1.capacity 2686976 1372848289\".");
     paramPrintStream.println("  <host>        The hostname to send Graphite data to, defaults to localhost.");
     paramPrintStream.println("  <protocol>    Protocol to send data over, defaults to tcp, udp not implemented yet");
   }
 
   public static void printVersion(PrintStream paramPrintStream) {
     URL localURL = Arguments.class.getClassLoader().getResource("version");
     try {
       BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localURL.openStream()));
 
       String str = null;
       while ((str = localBufferedReader.readLine()) != null)
         paramPrintStream.println(str);
     }
     catch (Exception localException)
     {
       System.err.println("Unexpected exception: " + localException.getMessage());
       localException.printStackTrace();
       System.exit(1);
     }
   }
 
   private static int toMillis(String paramString) throws IllegalArgumentException
   {
     String[] arrayOfString = { "ms", "s" };
 
     String str1 = null;
     String str2 = null;
 
     for (int i = 0; i < arrayOfString.length; i++) {
       int j = paramString.indexOf(arrayOfString[i]);
       if (j > 0) {
         str1 = paramString.substring(j);
         str2 = paramString.substring(0, j);
         break;
       }
     }
 
     if (str1 == null) str2 = paramString;
     try
     {
       int i = Integer.parseInt(str2);
 
       if ((str1 == null) || (str1.compareTo("ms") == 0)) {
         return i;
       }
       if (str1.compareTo("s") == 0) {
         return i * 1000;
       }
 
       throw new IllegalArgumentException("Unsupported interval time unit: " + str1);
     }
     catch (NumberFormatException localNumberFormatException)
     {
     }
     throw new IllegalArgumentException("Could not convert interval: " + paramString);
   }
 
   public Arguments(String[] paramArrayOfString)
   {
     int i = 0;
 
     if ((paramArrayOfString.length < 1) || (paramArrayOfString.length > 6)) {
       throw new IllegalArgumentException("invalid argument count");
     }
 
     if ((paramArrayOfString[0].compareTo("-?") == 0) || (paramArrayOfString[0].compareTo("-help") == 0)) {
       if (paramArrayOfString.length != 1) {
         throw new IllegalArgumentException("invalid argument count");
       }
       this.help = true;
       return;
     }
     if ((paramArrayOfString[0].compareTo("-v") == 0) || (paramArrayOfString[0].compareTo("-version") == 0))
     {
       this.version = true;
       return;
     }
 
     i = 0;
     Object localObject;
     if ((i < paramArrayOfString.length) && (paramArrayOfString[i].startsWith("-")))
     {
       String str = paramArrayOfString[i];
 
       localObject = null;
       int j = paramArrayOfString[i].indexOf('@');
       if (j < 0) {
         localObject = paramArrayOfString[i];
       }
       else {
         localObject = paramArrayOfString[i].substring(0, j);
       }
 
       try
       {
         int k = Integer.parseInt((String)localObject);
       }
       catch (NumberFormatException localNumberFormatException)
       {
         throw new IllegalArgumentException("illegal argument: " + paramArrayOfString[i]);
       }
 
     }
     /* ideally we should replace this by something smart... */
     switch (paramArrayOfString.length - i) {
     case 6:
        this.GraphiteProto = paramArrayOfString[(paramArrayOfString.length - 1)];
        this.GraphitePort = paramArrayOfString[(paramArrayOfString.length - 2)];
        this.GraphiteHost = paramArrayOfString[(paramArrayOfString.length - 3)];
        this.GraphitePrepend = paramArrayOfString[(paramArrayOfString.length - 4)];
        this.interval = toMillis(paramArrayOfString[(paramArrayOfString.length - 5)]);
        this.vmIdString = paramArrayOfString[(paramArrayOfString.length - 6)];
        break;
     case 5:
        this.GraphiteProto = "tcp";
        this.GraphitePort = paramArrayOfString[(paramArrayOfString.length - 1)];
        this.GraphiteHost = paramArrayOfString[(paramArrayOfString.length - 2)];
        this.GraphitePrepend = paramArrayOfString[(paramArrayOfString.length - 3)];
        this.interval = toMillis(paramArrayOfString[(paramArrayOfString.length - 4)]);
        this.vmIdString = paramArrayOfString[(paramArrayOfString.length - 5)];
        break;
     case 4:
        this.GraphiteProto = "tcp";
        this.GraphitePort = "2003";
        this.GraphiteHost = paramArrayOfString[(paramArrayOfString.length - 1)];
        this.GraphitePrepend = paramArrayOfString[(paramArrayOfString.length - 2)];
        this.interval = toMillis(paramArrayOfString[(paramArrayOfString.length - 3)]);
        this.vmIdString = paramArrayOfString[(paramArrayOfString.length - 4)];
        break;
     case 3:
       this.FileName = paramArrayOfString[(paramArrayOfString.length - 1)];
       this.interval = toMillis(paramArrayOfString[(paramArrayOfString.length - 2)]);
       this.vmIdString = paramArrayOfString[(paramArrayOfString.length - 3)];
       break;
     case 2:
       this.interval = toMillis(paramArrayOfString[(paramArrayOfString.length - 1)]);
       this.vmIdString = paramArrayOfString[(paramArrayOfString.length - 2)];
       break;
     case 1:
       this.vmIdString = paramArrayOfString[(paramArrayOfString.length - 1)];
       break;
     }
 
     if (this.interval == -1) this.interval = 500;
     try
     {
       this.vmId = new VmIdentifier(this.vmIdString);
     
     }
     catch (URISyntaxException e) {
        System.out.println("ARGH!");
     }// throw
   }
 
   public boolean isHelp() {
     return this.help;
   }
 
   public boolean isVersion() {
     return this.version;
   }
 
   public String vmIdString() {
     return this.vmIdString;
   }
 
   public VmIdentifier vmId() {
     return this.vmId;
   }
 
   public int samplingInterval() {
     return this.interval;
   }

   public String FileName() {
     return this.FileName;
   }

    public String GraphitePort() {
        return this.GraphitePort;
    }

    public String GraphiteHost() {
        return this.GraphiteHost;
    }

    public String GraphiteProto() {
        return this.GraphiteProto;
    }
    public String GraphitePrepend() {
        return this.GraphitePrepend;
    }
 }

/* Location:           C:\Users\fkessen\jvmstat-3_0\jvmstat\jars\visualgc.jar
 * Qualified Name:     com.sun.jvmstat.tools.visualgc.Arguments
 * JD-Core Version:    0.6.0
 */
