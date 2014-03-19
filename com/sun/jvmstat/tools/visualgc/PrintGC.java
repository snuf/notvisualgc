package com.sun.jvmstat.tools.visualgc;
 
import com.sun.jvmstat.graph.FIFOList;
import com.sun.jvmstat.graph.Line;
import com.sun.jvmstat.util.Converter;
import java.util.Map;
import java.util.HashMap;
import java.lang.Long;

// perhaps rename the class to something more sensible... 
class PrintGC {
   public FIFOList gcActiveDataSet;
   public FIFOList finalizerActiveDataSet;
   public FIFOList finalizerQLengthDataSet;
   public FIFOList compilerActiveDataSet;
   public FIFOList classLoaderActiveDataSet;
   private boolean inGC = false;
   private boolean inEdGC = false;
   private boolean inTnGC = false;
   // private long edenGCStart;
   // private long tenuredGCStart;
   private long maxFinalizerQLength;
   private boolean inCL = false;
   private long clStart;
   private boolean inComp = false;
   private long compStart;
   private boolean run;
   private String State = "_Unknown_";
   private GCSample previousSample;
   private GCSample Sample;
   private Long osFrequency;
   private Long msTimeStamp = System.currentTimeMillis();

   // Rename later prepares everything for output
   public PrintGC(GCSample paramGCSample, GCSample previousSample) {
     this.Sample = paramGCSample;
     // figure out samples and JVM state
     if (previousSample == null) {
	this.previousSample = this.Sample;
     } else {
    	this.previousSample = previousSample;
        if (this.Sample.osElapsedTime == this.previousSample.osElapsedTime) {
            this.State = "_Halted_";
        } else {
            this.State = "_Alive_";
        }
     }
     this.maxFinalizerQLength = Math.max(this.maxFinalizerQLength, paramGCSample.finalizerQLength);
     this.gcActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
     this.classLoaderActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
     this.compilerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
     this.finalizerActiveDataSet = new FIFOList(1000, 0.0D, 1.0D);
     this.finalizerQLengthDataSet = new FIFOList(1000); 
     this.osFrequency = this.Sample.osFrequency;
   }
  
   public String ReturnHeader() {
       String Str = "ts, os, fr, " +
		"jst, jrt, " +
		"cmp, cmpt, " +
		"cll, clu, clt, " +
		"fm, fc, flm, fo, ft, " +
		"edc, edu, edgc, edt, " +
		"sv0c sv0u, sv1c, sv1u, " +
		"tc, tu. tgc, tt, " +
		"pc, pu, " +
		// gcte, gctt can also be removed ?
		"gc, gce, gct, gctc, gctt, gcc";
	return Str;
   }
   
   /* return a named list of counters that can be traversed through */
   public Map<String, Long> ReturnMap() {
        Map<String, Long> data = new HashMap<String, Long>();
        /* fill the map, might need to divide some stuff by GCSample.osFrequency */
        data.put("run.interval", this.Sample.osElapsedTime -  this.previousSample.osElapsedTime);
        data.put("run.time", this.Sample.osElapsedTime);
        data.put("finalizer.queue.current",  this.Sample.finalizerQMaxLength);
        data.put("finalizer.queue.max", this.Sample.finalizerQLength);
        data.put("finalizer.queue.local_max", this.maxFinalizerQLength);
        data.put("finalizer.count", this.Sample.finalizerCount);
        data.put("finalizer.time", this.Sample.finalizerTime);
        data.put("compile.time", this.Sample.totalCompile);
        data.put("compile.count", this.Sample.totalCompileTime);
        data.put("classloader.loaded",this.Sample.classesLoaded);
        data.put("classloader.unloadded", this.Sample.classesUnloaded);
        data.put("classloader.time", this.Sample.classLoadTime);
        /* we leave gcCause out for now...  this.Sample.lastGCCause, could use length =) */
        /* to get total GC stuff one needs to add tenured and eden */
        data.put("perm.capacity", this.Sample.permCapacity);
        data.put("perm.used", this.Sample.permUsed);
        data.put("tenured.capcity", this.Sample.tenuredCapacity);
        data.put("tenured.used", this.Sample.tenuredUsed);
        data.put("tenured.gc", this.Sample.tenuredGCEvents);
        data.put("tenured.gctime", this.Sample.tenuredGCTime);
        data.put("eden.capacity", this.Sample.edenCapacity);
        data.put("eden.used", this.Sample.edenUsed);
        data.put("eden.gc", this.Sample.edenGCEvents);
        data.put("eden.gctime", this.Sample.edenGCTime);
        data.put("survivor.0.capacity", this.Sample.survivor0Capacity);
        data.put("survivor.0.used", this.Sample.survivor0Used);
        data.put("survivor.1.capacity", this.Sample.survivor1Capacity);
        data.put("survivor.1.used", this.Sample.survivor1Used);
        return data;
   }

   public String ReturnLine() {
       /* start of string building */
       String Str = msTimeStamp + ", " +
		(this.Sample.osElapsedTime - this.previousSample.osElapsedTime) + ", " +
		GCSample.osFrequency + ", " +
		this.State + ", " +
		Converter.longToTimeString(this.Sample.osElapsedTime, GCSample.osFrequency) + ", " +
		this.Sample.totalCompile + ", " +
		Converter.longToTimeString(this.Sample.totalCompileTime, GCSample.osFrequency) + ", " +
		/* classloader */
		this.Sample.classesLoaded + ", " +
		this.Sample.classesUnloaded + ", "  +
		Converter.longToTimeString(this.Sample.classLoadTime, GCSample.osFrequency) + ", " +
		/* finalizer */
		this.Sample.finalizerQMaxLength + ", " +
		this.Sample.finalizerQLength + ", " +
		this.maxFinalizerQLength + ", " +
                this.Sample.finalizerCount + ", " +
                Converter.longToTimeString(this.Sample.finalizerTime, GCSample.osFrequency) + ", " +
		/* eden */
		this.Sample.edenCapacity + ", " +
		this.Sample.edenUsed + ", " +
		this.Sample.edenGCEvents + ", " +
		this.Sample.edenGCTime + ", " +
		/* survivor */
		this.Sample.survivor0Capacity + ", " +
		this.Sample.survivor0Used + ", " +
		this.Sample.survivor1Capacity + ", " +
		this.Sample.survivor1Used + ", " +
		/* tenured */
		this.Sample.tenuredCapacity + ", " +
		this.Sample.tenuredUsed + ", " +
		this.Sample.tenuredGCEvents + ", " +
		this.Sample.tenuredGCTime + ", " +
		/* perm */
		this.Sample.permCapacity + ", " +
		this.Sample.permUsed + ", " +
		/* GC ing */
		(this.Sample.edenGCEvents + this.Sample.tenuredGCEvents) + ", " +
		// might want to keep these two out?
		this.Sample.edenGCEvents + ", " +
		this.Sample.tenuredGCEvents + ", " +
		Converter.longToTimeString(this.Sample.edenGCTime + this.Sample.tenuredGCTime, GCSample.osFrequency) + ", " +
		Converter.longToTimeString(this.Sample.edenGCTime, GCSample.osFrequency) + ", " +
		Converter.longToTimeString(this.Sample.tenuredGCTime, GCSample.osFrequency) + ", " +
		this.Sample.lastGCCause;
        return Str;
   }
 
   public boolean getRun() {
     return this.run;
   }
 
   public void setRun(boolean paramBoolean) {
     this.run = paramBoolean;
   }
 
   /* already contains a string, reuse */
   public void printTextComponents() {
     // osElapsedTime can be used to look at the correct interval.. osFrequency is a usefull divider to show a real time...
     System.out.println("* Time: "+ msTimeStamp +"ms elapsed: " + this.Sample.osElapsedTime + " - " + this.previousSample.osElapsedTime);

     String str = "State: " + this.State +
		" Run time: " + 
		Converter.longToTimeString(this.Sample.osElapsedTime, GCSample.osFrequency);
     System.out.println(str);

     str = "Finalizer Queue Length: Maximum " + 
		this.Sample.finalizerQMaxLength + 
		" Current " + this.Sample.finalizerQLength + 
		" Local Maximum " + this.maxFinalizerQLength;
     System.out.println(str);
 
     str = "Finalizer Time: " + 
		this.Sample.finalizerCount + 
		" objects - " + 
		Converter.longToTimeString(this.Sample.finalizerTime, GCSample.osFrequency);
     System.out.println(str);
 
     str = "Compile Time: " + 
		this.Sample.totalCompile + 
		" compiles - " + 
		Converter.longToTimeString(this.Sample.totalCompileTime, GCSample.osFrequency);
     System.out.println(str);
 
     str = "Class Loader Time: " + 
		this.Sample.classesLoaded + 
		" loaded, " + 
		this.Sample.classesUnloaded + 
		" unloaded - " + 
		Converter.longToTimeString(this.Sample.classLoadTime, GCSample.osFrequency);
     System.out.println(str);
 
     str = "GC Time: " + 
		(this.Sample.edenGCEvents + this.Sample.tenuredGCEvents) + 
		" collections, " + 
		Converter.longToTimeString(this.Sample.edenGCTime + this.Sample.tenuredGCTime, GCSample.osFrequency);

     if ((this.Sample.lastGCCause != null) && (this.Sample.lastGCCause.length() != 0)) {
       str = str + " Last Cause: " + this.Sample.lastGCCause;
     } else {
	str = str + " Last Cause: No GC";
     }
     System.out.println(str);

     str = "Perm Capacity: " + this.Sample.permCapacity + " Used " + this.Sample.permUsed;
     System.out.println(str);

     str = "Tenured Capacity: " + 
		this.Sample.tenuredCapacity + 
		" Used " + this.Sample.tenuredUsed + 
		" GC Events " + this.Sample.tenuredGCEvents + 
		" GC Time " + this.Sample.tenuredGCTime + " OS Frequency " 
		+ this.osFrequency;
     System.out.println(str);
			  
     str = "Eden Capacity: " + 
 		this.Sample.edenCapacity + 
		" Used " + this.Sample.edenUsed + 
		" GC Events " + this.Sample.edenGCEvents + 
		" GC Time " + this.Sample.edenGCTime + " OS Frequency " + 
		this.osFrequency;
     System.out.println(str);
			  
     str = "Surviover0 Capacity: " + this.Sample.survivor0Capacity + " Used " + this.Sample.survivor0Used;
     System.out.println(str);
			  
     str = "Surviover1 Capacity: " + this.Sample.survivor1Capacity + " Used " + this.Sample.survivor1Used;
     System.out.println(str);

   }
}
