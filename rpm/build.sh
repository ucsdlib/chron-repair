#!/bin/bash

rpmdir=$PWD
sources=SOURCES
finaljar=$sources/chron-repair.jar
retval=0

if [ "$1" = "clean" ]; then
    echo "Cleaning"
    rm -rf BUILD/
    rm -rf BUILDROOT/
    rm -rf RPMS/
    rm -rf SRPMS/
    rm -rf SOURCES/
    rm -rf tmp/
    exit 0
fi

if [ ! -d $sources ]; then
    mkdir $sources
fi

cd ../

# Get the version of the build and trim off the -SNAPSHOT
echo "Getting version from maven..."
full_version=`mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec`

# Not the best regex but since it's small it shouldn't matter much
version=`echo $full_version | sed 's/-.*//'`
release_type=`echo $full_version | sed 's/.*-//'`

if [ $? -ne 0 ]; then
    echo "Error getting version from maven exec plugin"
    exit
fi

# Only package releases
jarfile=target/medic-$version-$release_type.jar

if [ ! -e $jarfile ]; then
    echo "Building latest jar..."
    mvn -q clean install # > /dev/null
    if [ $? -ne 0 ]; then
        echo "Error building chron-repair"
        exit
    fi
else
    echo "Jar already built"
fi

# Copy the artifacts
cp $jarfile rpm/$finaljar
cp target/classes/application.yml rpm/$sources/repair.yml

# cd back to where we started and build the rpm
cd $rpmdir
cp chron-repair.sh $sources

rpmbuild -ba --define="_topdir $PWD" --define="_tmppath $PWD/tmp" --define="ver $version" SPECS/repair.spec
