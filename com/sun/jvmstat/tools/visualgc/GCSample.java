 package com.sun.jvmstat.tools.visualgc;
 
 public class GCSample
 {
   long newGenMaxSize;
   long newGenMinSize;
   long newGenCurSize;
   long edenSize;
   long edenCapacity;
   long edenUsed;
   long edenGCEvents;
   long edenGCTime;
   long survivor0Size;
   long survivor0Capacity;
   long survivor0Used;
   long survivor1Size;
   long survivor1Capacity;
   long survivor1Used;
   long tenuredSize;
   long tenuredCapacity;
   long tenuredUsed;
   long tenuredGCEvents;
   long tenuredGCTime;
   long permSize;
   long permCapacity;
   long permUsed;
   long tenuringThreshold;
   long desiredSurvivorSize;
   long[] ageTableSizes;
   long classLoadTime;
   long classesLoaded;
   long classesUnloaded;
   long classBytesLoaded;
   long classBytesUnloaded;
   long totalCompileTime;
   long totalCompile;
   boolean finalizerInitialized;
   long finalizerTime;
   long finalizerCount;
   long finalizerQLength;
   long finalizerQMaxLength;
   long osElapsedTime;
   long lastModificationTime;
   String lastGCCause;
   String currentGCCause;
   static long maxTenuringThreshold;
   static long osFrequency;
   static String javaCommand;
   static String javaHome;
   static String vmArgs;
   static String vmFlags;
   static String vmInfo;
   static String vmName;
   static String vmVersion;
   static String vmVendor;
   static String vmSpecName;
   static String vmSpecVersion;
   static String vmSpecVendor;
   static String classPath;
   static String bootClassPath;
   static String libraryPath;
   static String bootLibraryPath;
   static String endorsedDirs;
   static String extDirs;
   private static boolean initialized = false;
 
   static synchronized void initStaticCounters(Model paramModel)
   {
     if (!initialized) {
       maxTenuringThreshold = paramModel.getMaxTenuringThreshold();
       osFrequency = paramModel.getOsFrequency();
 
       javaCommand = paramModel.getJavaCommand();
       javaHome = paramModel.getJavaHome();
       vmArgs = paramModel.getVmArgs();
       vmFlags = paramModel.getVmFlags();
       vmInfo = paramModel.getVmInfo();
       vmName = paramModel.getVmName();
       vmVersion = paramModel.getVmVersion();
       vmVendor = paramModel.getVmVendor();
       vmSpecName = paramModel.getVmSpecName();
       vmSpecVersion = paramModel.getVmSpecVersion();
       vmSpecVendor = paramModel.getVmSpecVendor();
       classPath = paramModel.getClassPath();
       bootClassPath = paramModel.getBootClassPath();
       libraryPath = paramModel.getLibraryPath();
       bootLibraryPath = paramModel.getBootLibraryPath();
       endorsedDirs = paramModel.getEndorsedDirs();
       extDirs = paramModel.getExtDirs();
     }
   }
 
   GCSample(Model paramModel)
   {
     initStaticCounters(paramModel);
 
     this.newGenMaxSize = paramModel.getNewGenMaxSize();
     this.newGenMinSize = paramModel.getNewGenMinSize();
     this.newGenCurSize = paramModel.getNewGenCurSize();
 
     this.edenUsed = paramModel.getEdenUsed();
     this.survivor0Used = paramModel.getSurvivor0Used();
     this.survivor1Used = paramModel.getSurvivor1Used();
     this.tenuredUsed = paramModel.getTenuredUsed();
     this.permUsed = paramModel.getPermUsed();
 
     this.tenuringThreshold = paramModel.getTenuringThreshold();
 
     this.edenSize = paramModel.getEdenSize();
     this.survivor0Size = paramModel.getSurvivor0Size();
     this.survivor1Size = paramModel.getSurvivor1Size();
     this.tenuredSize = paramModel.getTenuredSize();
     this.permSize = paramModel.getPermSize();
 
     this.edenCapacity = paramModel.getEdenCapacity();
     this.survivor0Capacity = paramModel.getSurvivor0Capacity();
     this.survivor1Capacity = paramModel.getSurvivor1Capacity();
     this.tenuredCapacity = paramModel.getTenuredCapacity();
     this.permCapacity = paramModel.getPermCapacity();
 
     this.edenGCEvents = paramModel.getEdenGCEvents();
     this.edenGCTime = paramModel.getEdenGCTime();
     this.tenuredGCEvents = paramModel.getTenuredGCEvents();
     this.tenuredGCTime = paramModel.getTenuredGCTime();
 
     this.tenuringThreshold = paramModel.getTenuringThreshold();
     this.desiredSurvivorSize = paramModel.getDesiredSurvivorSize();
     this.ageTableSizes = paramModel.getAgeTableSizes();
     this.lastGCCause = paramModel.getLastGCCause();
     this.currentGCCause = paramModel.getCurrentGCCause();
 
     this.classLoadTime = paramModel.getClassLoadTime();
     this.classesLoaded = paramModel.getClassesLoaded();
     this.classesUnloaded = paramModel.getClassesUnloaded();
     this.classBytesLoaded = paramModel.getClassBytesLoaded();
     this.classBytesUnloaded = paramModel.getClassBytesUnloaded();
 
     this.totalCompileTime = paramModel.getTotalCompileTime();
     this.totalCompile = paramModel.getTotalCompile();
 
     paramModel.initializeFinalizer();
     this.finalizerInitialized = paramModel.isFinalizerInitialized();
     this.finalizerTime = paramModel.getFinalizerTime();
     this.finalizerCount = paramModel.getFinalizerCount();
     this.finalizerQLength = paramModel.getFinalizerQLength();
     this.finalizerQMaxLength = paramModel.getFinalizerQMaxLength();
 
     this.osElapsedTime = paramModel.getOsElapsedTime();
     this.lastModificationTime = paramModel.getLastModificationTime();
   }
 
   public double getAdjustedEdenSize() {
     if (this.edenCapacity + this.survivor0Capacity + this.survivor1Capacity == this.newGenMaxSize)
     {
       return this.edenCapacity;
     }
 
     long l = this.newGenMaxSize - this.newGenCurSize;
     return this.edenCapacity + l;
   }
 
   public double getEdenLiveRatio()
   {
     return this.edenUsed / this.edenSize;
   }
 
   public double getAdjustedEdenLiveRatio() {
     return this.edenUsed / getAdjustedEdenSize();
   }
 
   public double getSurvivor0LiveRatio() {
     return this.survivor0Used / this.survivor0Size;
   }
 
   public double getSurvivor1LiveRatio() {
     return this.survivor1Used / this.survivor1Size;
   }
 
   public double getTenuredLiveRatio() {
     return this.tenuredUsed / this.tenuredSize;
   }
 
   public double getPermLiveRatio() {
     return this.permUsed / this.permSize;
   }
 
   public double getAdjustedEdenCommittedRatio() {
     return this.edenCapacity / getAdjustedEdenSize();
   }
 
   public double getEdenCommittedRatio() {
     return this.edenCapacity / this.edenSize;
   }
 
   public double getSurvivor0CommittedRatio() {
     return this.survivor0Capacity / this.survivor0Size;
   }
 
   public double getSurvivor1CommittedRatio() {
     return this.survivor1Capacity / this.survivor1Size;
   }
 
   public double getTenuredCommittedRatio() {
     return this.tenuredCapacity / this.tenuredSize;
   }
 
   public double getPermCommittedRatio() {
     return this.permCapacity / this.permSize;
   }
 
   public long getTotalGCTime(GCSample paramGCSample) {
     return Math.abs(this.edenGCTime - paramGCSample.edenGCTime) + Math.abs(this.tenuredGCTime - paramGCSample.tenuredGCTime);
   }
 
   public boolean heapSizeChanged(GCSample paramGCSample)
   {
     return (this.edenCapacity != paramGCSample.edenCapacity) || (this.survivor0Capacity != paramGCSample.survivor0Capacity) || (this.survivor1Capacity != paramGCSample.survivor1Capacity) || (this.tenuredCapacity != paramGCSample.tenuredCapacity) || (this.permCapacity != paramGCSample.permCapacity);
   }
 }

/* Location:           C:\Users\fkessen\jvmstat-3_0\jvmstat\jars\visualgc.jar
 * Qualified Name:     com.sun.jvmstat.tools.visualgc.GCSample
 * JD-Core Version:    0.6.0
 */