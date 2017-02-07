timeStart=$(($(date +%s)))
vCode=""
vName=""
#定义输出目录名称
packageDir=`date '+%Y%m%d%H%M'`
mkdir -p ~/tesst/package/$packageDir/channel
outputDir=~/tesst/package/
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


gradle -Pmarket=markets.txt clean assemble_yf
mv app/build/outputs/apk/app-_yf-release.apk "$outputDir"$packageDir/com.cylan.jiafeigou.yf_release_v$vName-$vCode.apk
mv app/build/outputs/apk/app-_yf-debug.apk "$outputDir"$packageDir/com.cylan.jiafeigou.yf_debug_v$vName-$vCode.apk

#生成云渠道包
./gradlew -Pmarket=markets.txt clean apk_yunRelease
cp -r build/archives/* "$outputDir"$packageDir/channel
#生成doby渠道包

./gradlew -Pmarket=markets.txt clean apk_zhongxingRelease
cp -r build/archives/* "$outputDir"$packageDir/channel

timeEnd=$(($(date +%s)))
echo "耗时: "$(($timeEnd - $timeStart))"s"