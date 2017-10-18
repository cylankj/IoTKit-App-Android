#!/usr/bin/env bash
#这个需要指定本机CleverDog 的目录,否则需要从头开始下载
RootPath=$(cd `dirname $0`; pwd)

if [ ! -n "$1"  ]; then
  CleverDog="./CleverDog"
else
  CleverDog=$1
fi
if [ ! -d "$CleverDog" ]; then
   git clone -b master http://120.24.247.124:10080/Sam01/CleverDog.git
fi
cd ${CleverDog}
git checkout -B master
git pull

if [ ! -d "./Language" ]; then
  echo "当前目录下没有 Language 文件夹 导入语言包失败"
  exit 0
fi
cd ./Language
if [ ! -d "./output" ]; then
    mkdir -p ./output
fi
java -jar jfglanguage_android.jar ./xls文档/3.0/language-3.0.xls output

cd ./output

if [ -d "./cylan" ]; then
    echo "正在复制 cylan 文件夹下的语言包"
    cp -rf ./cylan/* ${RootPath}/app/src/main/res/
fi

if [ -d "./cell_c" ]; then
    echo "正在复制 cell_c 文件夹下的语言包"
    if [ ! -d "${RootPath}/app/src/_cell_c/res" ]; then
        mkdir -p ${RootPath}/app/src/_cell_c/res
    fi
       cp -rf ./cell_c/* ${RootPath}/app/src/_cell_c/res/
fi

if [ -d "./doby" ]; then
    echo "正在复制 doby 文件夹下的语言包"
    if [ ! -d "${RootPath}/app/src/_doby/res" ]; then
        mkdir -p ${RootPath}/app/src/_doby/res
    fi
    cp -rf ./doby/* ${RootPath}/app/src/_doby/res
fi
echo "自动更新语言包完成了!!!"