timeStart=$(($(date +%s)))
vCode=""
vName=""
#定义输出目录名称
packageDir=`date '+%Y%m%d%H%M'`
mkdir -p $1/$packageDir/channel
outputDir=$1
#!/bin/bash
while IFS='' read -r line || [[ -n "$line" ]]; do
#提取versionCode
    if [[ $line == *"versionCode"* ]]; then
      vCode=`echo $line | sed -e "s/versionCode=//g"`
      echo "vCode:" $vCode
    fi
#提取versionName
    if [[ $line == *"versionName"* ]]; then
      vName=`echo $line | sed -e "s/versionName=//g"`
      echo "vCode:" $vName
    fi
done <version.properties

./gradlew assemble_test1
mkdir -p $outputDir
mv app/build/outputs/apk/app-_test1-release.apk "$outputDir"/com.cylan.jiafeigou.test1_release_v$vName-$vCode.apk
mv app/build/outputs/apk/app-_test1-debug.apk "$outputDir"/com.cylan.jiafeigou.test1_debug_v$vName-$vCode.apk


timeEnd=$(($(date +%s)))
echo "耗时: "$(($timeEnd - $timeStart))"s"