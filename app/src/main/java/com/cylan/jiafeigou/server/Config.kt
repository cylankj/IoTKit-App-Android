package com.cylan.jiafeigou.server

/**
 * Created by yanzhendong on 2017/8/22.
 */

enum class PAGE_MESSAGE(private val messages: List<Int>) {
    /*这里的消息是针对页面的,不是针对具体的 OS 的 ,还会有一个 OS 属性表,进行筛选*/
    PAGE_HOME(listOf(201, 202, 206, 508, 501, 1001, 1002, 1003, 1004, 1005)),
    PAGE_SETTING(listOf(201, 202, 204, 209, 222/*这个需要确定是否主动请求*/, 223, 216, 217, 225, 226, 303, 304, 501, 508, 509)),
    PAGE_CAMERA(listOf(201, 202, 204, 207, 214, 218, 222, 303, 501, 508, 509, 510, 513, 1001, 1002, 1003, 1004, 1005)),
    PAGE_SETTING_DETAIL(listOf(201, 204, 205, 206, 207, 208, 210, 214, 217, 222/*需要考虑下是否加上这个消息*/)),
    PAGE_SAFETY_PROTECTION(listOf(204, 303, 501, 502, 503, 504, 514, 515)),
    PAGE_RECORD_SETTING(listOf(204, 303, 305, 501, 508)),
    PAGE_SDCARD_DETAIL(listOf(201, 202, 204));


    fun filter(other: List<Int>?) = messages.filter { other?.contains(it) ?: true }

}

/**
 * (Pid)	设备型号(Device)	CID起始号(cidPrefix)	OS(os)
——	DOG-1W-V2	2000	7
1090	DOG-1W-V1	2000	5
——	DOG-1W-V1	2200	5
——	DOG-1W-V2	2100	7
1071	DOG-72	3000	4
1092	DOG-2W-V2	2801	10
1091	DOG-2W	2800	18
1301	DOG-2W-V3	2802	36
1302	DOG-2W-V4	2803	37
1406	DOG-8W	2805	82
1089	DOG-5W	2900	21
1088	DOG-6W-V1	2600	26
1093	DOG-ML	5000	6
1094	DOG-DB-V2	5100	25
1379	DOG-DB-V3	5200	52
——	DOG-iHome	7000	8
——	DOG-EN-MG	7300	11
1152	DOG-1W-V1	6000	5
1158	DOG-CAM-CC3200	6500	17
1159	DOG-BELL-V2	6900	15
1160	LS-ZNMY-V1	6901	27
1375	DOG-4W	2700	20
1375	DOG-4W	6002	20
——	DOG-3W-V2	6004	23
——	DOG-3W	6001	19
1285	RS-CAM	6006	38
1284	RS-CAM-V2	6007	39
1385	RS-T826	6010	81
1348	RS-T824-A0E	6008	49
1378	RS-V860J-A0-A	6009	50
——	DOG-1W-V1	6200	5
——	DOG-BELL	6600	6
——	DOG-1W-V1	6400	5
——	DOG-BELL	6003	6
——	DOG-BELL	6700	22
1090	DOG-1W-V1	6300	5
——	PSD-BELL	6005	24
1090	DOG-1W-V1	6100	5
——	ZNWS-BELL-LOCK	6800	28
1344	CeS-BELL-V1	2101	44
1345	CeS-BELL-V2	2102	46
1346	CeS-2W-V1	2103	47
1347	CeS-2W-V2	2104	48
1343	KKS-BELL-V2	6903	42
 * */
enum class OS_PROPERTY(val os: Int = -1, val pid: Int = -1, val cidPrefix: String = "", val properties: List<Int> = listOf(), val shareProperties: List<Int> = listOf()) {

    OS_DOG_1W_V2_2000_7(pid = 7, cidPrefix = "2000", properties = listOf(201)),
    OS_1090_DOG_1W_V1_2000_5(os = 1090, pid = 5, cidPrefix = "2000"),
    OS_DOG_1W_V1_2200_5(pid = 5, cidPrefix = "2200"),
    OS_DOG_1W_V2_2100_7(pid = 7, cidPrefix = "2100"),
    OS_1071_DOG_72_3000_4(pid = 1071, os = 4, cidPrefix = "3000"),
    OS_1092_DOG_2W_V2_2801_10(pid = 1092, os = 10, cidPrefix = "2801"),
    OS_1091_DOG_2W_2800_18(pid = 1091, os = 18, cidPrefix = "2800"),
    OS_1301_DOG_2W_V3_2802_36(pid = 1301, os = 36, cidPrefix = "2802"),
    OS_1302_DOG_2W_V4_2803_37(pid = 1302, os = 37, cidPrefix = "2803"),
    OS_1406_DOG_8W_2805_82(pid = 1406, os = 82, cidPrefix = "2805"),
    OS_1089_DOG_5W_2900_21,
    OS_1088_DOG_6W_V1_2600_26,
    OS_1093_DOG_ML_5000_6,
    OS_1094_DOG_DB_V2_5100_25,
    OS_1379_DOG_DB_V3_5200_52,
    OS_DOG_iHome_7000_8,
    OS_DOG_EN_MG_7300_11,
    OS_1152_DOG_1W_V1_6000_5,
    OS_1158_DOG_CAM_CC3200_6500_17,
    OS_1159_DOG_BELL_V2_6900_15,
    OS_1160_LS_ZNMY_V1_6901_27,
    OS_1375_DOG_4W_2700_20,
    OS_1375_DOG_4W_6002_20,
    OS_DOG_3W_V2_6004_23,
    OS_DOG_3W_6001_19,
    OS_1285_RS_CAM_6006_38,
    OS_1284_RS_CAM_V2_6007_39,
    OS_1385_RS_T826_6010_81,
    OS_1348_RS_T824_A0E_6008_49,
    OS_1378_RS_V860J_A0_A_6009_50,
    OS_DOG_1W_V1_6200_5,
    OS_DOG_BELL_6600_6,
    OS_DOG_1W_V1_6400_5,
    OS_DOG_BELL_6003_6,
    OS_DOG_BELL_6700_22,
    OS_1090_DOG_1W_V1_6300_5,
    OS_PSD_BELL_6005_24,
    OS_1090_DOG_1W_V1_6100_5,
    OS_ZNWS_BELL_LOCK_6800_28,
    OS_1344_CeS_BELL_V1_2101_44,
    OS_1345_CeS_BELL_V2_2102_46,
    OS_1346_CeS_2W_V1_2103_47,
    OS_1347_CeS_2W_V2_2104_48,
    OS_1343_KKS_BELL_V2_6903_42;

}

fun getMessageByOS(uuid: String?, share: Boolean = false): List<Int> = getOSType(uuid).let { if (share) it.shareProperties else it.properties }

fun getOSType(uuid: String?) = OS_PROPERTY.values().first { (uuid ?: "").startsWith(it.cidPrefix, true) }