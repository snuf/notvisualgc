#!/bin/sh
cps="/usr/lib/jvm/default-java /usr/local/openjdk6 /usr/local/diablo-jdk1.6.0 /Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home"
cp=""
for ncp in $cps
do
	if [ -f "${ncp}/lib/tools.jar" ]; then
		cp="${ncp}/lib/tools.jar"
		break
	fi
done

jars="$cp:jars/jvmstat_graph.jar:jars/jvmstat_util.jar:jars/visualgc.jar"
javac_args="-Xlint:deprecation -cp $jars:."
classes="Arguments File ReSocket PrintGC NotVisualGC NotVisualGCMain NotVisualGCAgent"
jar="jar umf com/META-INF/MANIFEST.MF jars/visualgc.jar"
for class in $classes
do
	x=`find ./ -name ${class}.java`
	javac $javac_args $x
	if [ "$?" != "0" ]
	then
		echo "ooops: $x broke"
		exit 1
	else 
		y=`find ./ -name ${class}*.class`
		
		echo "* Adding $y to $jar"
		$jar $y
		echo ""
	fi
done
# extra
# $jar com/sun/jvmstat/tools/visualgc/NotVisualGC\$1.class
