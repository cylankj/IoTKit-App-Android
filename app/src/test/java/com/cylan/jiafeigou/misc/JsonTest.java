package com.cylan.jiafeigou.misc;

import com.google.gson.Gson;

import org.json.JSONException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * Created by hds on 17-5-28.
 */

public class JsonTest {

    public static final String content = "{\n" +
            "  \"pList\": [\n" +
            "    {\n" +
            "      \"serial\": \"WiFi摄像头\",\n" +
            "      \"product\": \"WiFi一代摄像头（6376）\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-1W\",\n" +
            "      \"cidPrefix\": \"2000\",\n" +
            "      \"os\": 7,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"WiFi摄像头\",\n" +
            "      \"product\": \"WiFi一代摄像头（8330）\",\n" +
            "      \"pid\": 1090,\n" +
            "      \"cidModel\": \"DOG-1W\",\n" +
            "      \"cidPrefix\": \"2000\",\n" +
            "      \"os\": 5,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"WiFi摄像头\",\n" +
            "      \"product\": \"WiFi一代摄像头（测试）\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-1W-V1\",\n" +
            "      \"cidPrefix\": \"2200\",\n" +
            "      \"os\": 5,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"WiFi摄像头\",\n" +
            "      \"product\": \"WiFi一代摄像头（无夜视）\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-1W-V2\",\n" +
            "      \"cidPrefix\": \"2100\",\n" +
            "      \"os\": 7,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"3G摄像头\",\n" +
            "      \"product\": \"3G摄像头\",\n" +
            "      \"pid\": 1071,\n" +
            "      \"cidModel\": \"DOG-72\",\n" +
            "      \"cidPrefix\": \"3000\",\n" +
            "      \"os\": 4,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"180°单鱼眼全景\",\n" +
            "      \"product\": \"海思全景180°（1080P）\",\n" +
            "      \"pid\": 1092,\n" +
            "      \"cidModel\": \"DOG-2W-V2\",\n" +
            "      \"cidPrefix\": \"2801\",\n" +
            "      \"os\": 10,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"180°单鱼眼全景\",\n" +
            "      \"product\": \"海思全景180°（960P）\",\n" +
            "      \"pid\": 1091,\n" +
            "      \"cidModel\": \"DOG-2W\",\n" +
            "      \"cidPrefix\": \"2800\",\n" +
            "      \"os\": 18,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"180°单鱼眼全景\",\n" +
            "      \"product\": \"海思全景180°（睿视）\",\n" +
            "      \"pid\": 1091,\n" +
            "      \"cidModel\": \"DOG-2W-V3\",\n" +
            "      \"cidPrefix\": \"2802\",\n" +
            "      \"os\": 36,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"180°单鱼眼全景\",\n" +
            "      \"product\": \"海思全景120°（睿视）\",\n" +
            "      \"pid\": 1091,\n" +
            "      \"cidModel\": \"DOG-2W-V4\",\n" +
            "      \"cidPrefix\": \"2803\",\n" +
            "      \"os\": 37,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"720°全景摄像头\",\n" +
            "      \"product\": \"720°全景摄像头\",\n" +
            "      \"pid\": 1089,\n" +
            "      \"cidModel\": \"DOG-5W\",\n" +
            "      \"cidPrefix\": \"2900\",\n" +
            "      \"os\": 21,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"0\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"0\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"0\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"看家王云相机\",\n" +
            "      \"product\": \"看家王云相机\",\n" +
            "      \"pid\": 1088,\n" +
            "      \"cidModel\": \"DOG-6W-V1\",\n" +
            "      \"cidPrefix\": \"2600\",\n" +
            "      \"os\": 26,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"门铃\",\n" +
            "      \"product\": \"门铃一代\",\n" +
            "      \"pid\": 1093,\n" +
            "      \"cidModel\": \"DOG-ML\",\n" +
            "      \"cidPrefix\": \"5000\",\n" +
            "      \"os\": 6,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"0\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"0\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"0\",\n" +
            "        \"uptime\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"0\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"门铃\",\n" +
            "      \"product\": \"门铃二代\",\n" +
            "      \"pid\": 1094,\n" +
            "      \"cidModel\": \"DOG-DB-V2\",\n" +
            "      \"cidPrefix\": \"5100\",\n" +
            "      \"os\": 25,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"0\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"0\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"0\",\n" +
            "        \"uptime\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"0\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"中控\",\n" +
            "      \"product\": \"中控\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-iHome\",\n" +
            "      \"cidPrefix\": \"7000\",\n" +
            "      \"os\": 8,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"0\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"protection\": \"0\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"0\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"0\",\n" +
            "        \"uptime\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"0\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"门磁\",\n" +
            "      \"product\": \"门磁\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-EN-MG\",\n" +
            "      \"cidPrefix\": \"7300\",\n" +
            "      \"os\": 11,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"0\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"0\",\n" +
            "        \"protection\": \"0\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"0\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"0\",\n" +
            "        \"uptime\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"0\",\n" +
            "        \"sight\": \"0\",\n" +
            "        \"24Record\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"乐视\",\n" +
            "      \"product\": \"乐视一代WiFi摄像头\",\n" +
            "      \"pid\": 1152,\n" +
            "      \"cidModel\": \"LS\",\n" +
            "      \"cidPrefix\": \"6000\",\n" +
            "      \"os\": 5,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"乐视\",\n" +
            "      \"product\": \"乐视门铃方案摄像头\",\n" +
            "      \"pid\": 1158,\n" +
            "      \"cidModel\": \"Freecam\",\n" +
            "      \"cidPrefix\": \"6500\",\n" +
            "      \"os\": 17,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"0\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"乐视\",\n" +
            "      \"product\": \"乐视有源门铃（未接入）\",\n" +
            "      \"pid\": 1159,\n" +
            "      \"cidModel\": \"LS-BELL\",\n" +
            "      \"cidPrefix\": \"6900\",\n" +
            "      \"os\": 15,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"乐视\",\n" +
            "      \"product\": \"乐视猫眼\",\n" +
            "      \"pid\": 1160,\n" +
            "      \"cidModel\": \"LS-ZNMY\",\n" +
            "      \"cidPrefix\": \"6901\",\n" +
            "      \"os\": 27,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"0\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"1\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"0\",\n" +
            "        \"battery\": \"0\",\n" +
            "        \"mac\": \"0\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"0\",\n" +
            "        \"uptime\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"0\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"国科\",\n" +
            "      \"product\": \"国科全景摄像头\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-4W\",\n" +
            "      \"cidPrefix\": \"2700\",\n" +
            "      \"os\": 20,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"国科\",\n" +
            "      \"product\": \"国科全景（莱克威尔）\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-4W\",\n" +
            "      \"cidPrefix\": \"6002\",\n" +
            "      \"os\": 20,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"乔安\",\n" +
            "      \"product\": \"乔安非全景摄像头\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-3W\",\n" +
            "      \"cidPrefix\": \"6004\",\n" +
            "      \"os\": 23,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"乔安\",\n" +
            "      \"product\": \"乔安全景摄像头\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"DOG-3W\",\n" +
            "      \"cidPrefix\": \"6001\",\n" +
            "      \"os\": 19,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"睿视\",\n" +
            "      \"product\": \"睿视摄像头120°\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"RS-CAM\",\n" +
            "      \"cidPrefix\": \"6006\",\n" +
            "      \"os\": 38,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"1\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"睿视\",\n" +
            "      \"product\": \"睿视摄像头180°\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"RS-CAM-V2\",\n" +
            "      \"cidPrefix\": \"6007\",\n" +
            "      \"os\": 39,\n" +
            "      \"propertyMap\": {\n" +
            "        \"sysVersion\": \"1\",\n" +
            "        \"softVersion\": \"0\",\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"warmSound\": \"0\",\n" +
            "        \"tz\": \"1\",\n" +
            "        \"led\": \"1\",\n" +
            "        \"battery\": \"1\",\n" +
            "        \"mac\": \"1\",\n" +
            "        \"hangup\": \"0\",\n" +
            "        \"fu\": \"1\",\n" +
            "        \"uptime\": \"1\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"sd\": \"1\",\n" +
            "        \"sight\": \"1\",\n" +
            "        \"24Record\": \"1\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"富威迪WiFi一代摄像头\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"FWD\",\n" +
            "      \"cidPrefix\": \"6200\",\n" +
            "      \"os\": 5,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"翰祺旺门铃门锁\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"HQW-BELL\",\n" +
            "      \"cidPrefix\": \"6600\",\n" +
            "      \"os\": 6,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"金力通WiFi一代摄像头\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"JLT-305\",\n" +
            "      \"cidPrefix\": \"6600\",\n" +
            "      \"os\": 5,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"金力通门铃\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"JLT-BELL\",\n" +
            "      \"cidPrefix\": \"6003\",\n" +
            "      \"os\": 6,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"serial\": \"\",\n" +
            "      \"product\": \"金鑫智慧科技智能猫眼\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"JXZH-ZNMY\",\n" +
            "      \"cidPrefix\": \"6700\",\n" +
            "      \"os\": 22,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"岷晟微一代WiFi摄像头\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"MSW\",\n" +
            "      \"cidPrefix\": \"6300\",\n" +
            "      \"os\": 5,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"普顺达门铃\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"PSD-BELL\",\n" +
            "      \"cidPrefix\": \"6005\",\n" +
            "      \"os\": 24,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"远视界一代WiFi摄像头\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"YSJ\",\n" +
            "      \"cidPrefix\": \"6100\",\n" +
            "      \"os\": 5,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"product\": \"智能维识门锁\",\n" +
            "      \"pid\": 0,\n" +
            "      \"cidModel\": \"ZNWS-BELL\",\n" +
            "      \"cidPrefix\": \"6800\",\n" +
            "      \"os\": 6,\n" +
            "      \"propertyMap\": {\n" +
            "        \"autoRecord\": \"0\",\n" +
            "        \"110V\": \"0\",\n" +
            "        \"wifi\": \"0\",\n" +
            "        \"standby\": \"0\",\n" +
            "        \"hangup\": \"0\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"version\": \"20170528\"\n" +
            "}";

    @Test
    public void testJson() throws JSONException, FileNotFoundException {
        File file = new File("app/src/main/assets/properties.json");

    }
}
