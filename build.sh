
echo "timeStart:"  $(($(date +%s)))
versionCode
while read line;do
	if [ "${versionCode}" = "true" ];then
		$versionCode=line
		echo $line
	fi
done <version.properties

echo "timeEnd:"  $(($(date +%s)))



