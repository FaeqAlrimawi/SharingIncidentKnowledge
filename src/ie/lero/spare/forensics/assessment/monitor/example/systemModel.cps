<?xml version="1.0" encoding="UTF-8"?>
<environment:EnvironmentDiagram xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:environment="http://www.example.org/environment">
  <asset xsi:type="environment:Building" name="lero" description="" mobility="FIXED" containedAssets="second_floor"/>
  <asset xsi:type="environment:Floor" name="second_floor" description="" mobility="FIXED" containedAssets="office_admin office_T24 office_T23 office_T25 office_T26 office_T27 office_T28 office_T29 office_T30 meetingRoom1 office_Manager hallway" parentAsset="lero"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T23-DN" name="SL_T23" parentAsset="office_T23"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T24-DN" name="SL_T24" parentAsset="office_T24"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T25-DN" name="SL_T25" parentAsset="office_T25"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T26-DN" name="SL_T26" parentAsset="office_T26"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T27-DN" name="SL_T27" parentAsset="office_T27"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T28-DN SL_T28-DN" name="SL_T28" parentAsset="office_T28"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T28-DN" name="SL_T28" parentAsset="office_T28"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T29-DN" name="SL_T29" parentAsset="office_T29"/>
  <asset xsi:type="environment:SmartLight" connections="SL_T30-DN" name="SL_T30" parentAsset="office_T30"/>
  <asset xsi:type="environment:SmartLight" connections="SL_Mngr-DN" name="SL_Mngr" parentAsset="office_Manager"/>
  <asset xsi:type="environment:SmartLight" connections="SL_admin-DN" name="SL_admin" parentAsset="office_admin"/>
  <asset xsi:type="environment:SmartLight" connections="SL_BN-DN" name="SL_BN" parentAsset="office_BN"/>
  <asset xsi:type="environment:SmartLight" connections="SL_hallway-DN" name="SL_hallway" parentAsset="hallway"/>
  <asset xsi:type="environment:SmartLight" connections="SL_MtgRm1-DN" name="SL_MtgRm1" parentAsset="meetingRoom1"/>
  <asset xsi:type="environment:SmartLight" connections="SL_MtgRm2-DN" name="SL_MtgRm2" parentAsset="meetingRoom2"/>
  <asset xsi:type="environment:HVAC" connections="HVAC-DN" name="AC_mngr" mobility="FIXED" parentAsset="office_Manager"/>
  <asset xsi:type="environment:FireAlarm" connections="FA-DN" name="FireAlarm1" parentAsset="hallway"/>
  <asset xsi:type="environment:Server" connections="Server-DN" name="Server1"/>
  <asset xsi:type="environment:Workstation" connections="Workstation-DN" name="Workstation1"/>
  <asset xsi:type="environment:BusNetwork" connections="SL_admin-DN SL_T25-DN SL_Mngr-DN SL_MtgRm2-DN SL_T27-DN SL_T28-DN HVAC-DN SL_MtgRm1-DN SL_T23-DN SL_T24-DN SL_BN-DN SL_T29-DN Workstation-DN SL_T30-DN SL_T26-DN AC_admin-DN FA-DN SL_hallway-DN Server-DN SL_T28-DN" name="busNetwork" description="" mobility="FIXED"/>
  <asset xsi:type="environment:Laptop" name="Laptop-manager" mobility="FIXED" containedAssets="SoftwareX" parentAsset="office_Manager"/>
  <asset xsi:type="environment:Application" name="SoftwareX" parentAsset="Laptop-manager"/>
  <asset xsi:type="environment:Desktop" connections="conn_1T23-IPNet" name="desktop1_T23" description="" parentAsset="office_T23"/>
  <asset xsi:type="environment:Desktop" connections="conn_2T23-IPNet" name="desktop2_T23"/>
  <asset xsi:type="environment:Desktop" name="desktop1_T24" parentAsset="office_T24"/>
  <asset xsi:type="environment:Desktop" connections="conn_2T22-IPNet" name="desktop2_T24" model=""/>
  <asset xsi:type="environment:Desktop" name="desktop1_T25" parentAsset="office_T25"/>
  <asset xsi:type="environment:Desktop" connections="conn_2T25-IPNet" name="desktop2_T25" parentAsset="office_T25" model=""/>
  <asset xsi:type="environment:Desktop" name="desktop1_T26" description="" parentAsset="office_T26"/>
  <asset xsi:type="environment:Desktop" connections="conn_2T26-IPNet" name="desktop2_T26" parentAsset="office_T26" model=""/>
  <asset xsi:type="environment:Desktop" name="desktop1_T27" parentAsset="office_T27" model=""/>
  <asset xsi:type="environment:Desktop" connections="conn_2T27-IPNet" name="desktop2_T27" parentAsset="office_T27"/>
  <asset xsi:type="environment:Desktop" connections="conn_1T28-IPNet" name="desktop1_T28" parentAsset="office_T28"/>
  <asset xsi:type="environment:Desktop" connections="conn_2T28-IPNet" name="desktop2_T28" parentAsset="office_T28"/>
  <asset xsi:type="environment:Desktop" connections="conn_1T29-IPNet" name="desktop1_T29" parentAsset="office_T29"/>
  <asset xsi:type="environment:Desktop" connections="conn_2T29-IPNet" name="desktop2_T29" parentAsset="office_T29"/>
  <asset xsi:type="environment:Desktop" name="desktop1_T30" parentAsset="office_T30"/>
  <asset xsi:type="environment:Desktop" name="desktop2_T30" parentAsset="office_T30"/>
  <asset xsi:type="environment:IPNetwork" connections="conn_2T22-IPNet conn_2T27-IPNet conn_1T29-IPNet conn_2T26-IPNet iPConn_net-AP3 conn_1T23-IPNet conn_2T29-IPNet iPConn_net-AP1 conn_2T23-IPNet conn_2T25-IPNet conn_1T28-IPNet conn_2T28-IPNet iPConn_net-AP2" name="IPnetwork1" mobility="FIXED" Protocol="TCP/IP" encryption="MACsec">
    <type name="Ethernet"/>
  </asset>
  <asset xsi:type="environment:Toilet" connections="mensToilet_hallway" name="mensToilet" description="" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:Toilet" connections="womensToilet_hallway" name="women'sToilet" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:Toilet" connections="disabledToilet_hallway" name="disabledToilet" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:Kitchen" connections="kitchen_hallway kitchen_hallway" name="kitchen" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:Stairs" connections="elevatorsArea-stairsA" name="stairsA" description="" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:Stairs" connections="stairsB-hallway" name="stairsB" description="" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:ElevatorsArea" connections="elevatorsArea-hallway elevatorsArea-stairsA" name="elevatorsArea" mobility="FIXED" containedAssets="elevator1" parentAsset="second_floor"/>
  <asset xsi:type="environment:Elevator" name="elevator1" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:CardReader" name="cardReader_ElevatorsArea-Hallway" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Hallway" connections="officeT24-hallway meetingRoom1-hallway informaticsLab_hallway2 officeT23-hallway kitchen_hallway openLab_hallway2 officeAdmin-hallway officeManager-hallway officeBN-hallway disabledToilet_hallway elevatorsArea-hallway mensToilet_hallway meetingRoom2-hallway stairsB-hallway officeT30-hallway officeT28-hallway officeT27-hallway kitchen_hallway officeT29-hallway officeT25-hallway officeT26-hallway womensToilet_hallway" name="hallway" mobility="FIXED" containedAssets="FireAlarm1" parentAsset="second_floor"/>
  <asset xsi:type="environment:Server" name="server1" mobility="FIXED"/>
  <asset xsi:type="environment:Server" name="server2" mobility="FIXED"/>
  <asset xsi:type="environment:Laptop" name="visitorLaptop" mobility="FIXED" containedAssets="malware" parentAsset="visitor1"/>
  <asset xsi:type="environment:Office" connections="officeAdmin-hallway" name="office_admin" mobility="FIXED" containedAssets="admin1 admin2 AC_admin" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT26-hallway" name="office_T26" mobility="FIXED" containedAssets="desktop1_T26 desktop2_T26" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT27-hallway" name="office_T27" mobility="FIXED" containedAssets="desktop1_T27 desktop2_T27" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT28-hallway" name="office_T28" mobility="FIXED" containedAssets="desktop1_T28 desktop2_T28" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT29-hallway" name="office_T29" mobility="FIXED" containedAssets="desktop1_T29 desktop2_T29" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT30-hallway" name="office_T30" mobility="FIXED" containedAssets="desktop1_T30 desktop2_T30" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeBN-hallway" name="office_BN" mobility="FIXED" containedAssets="smartTV-BN prof-BN" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeManager-hallway" name="office_Manager" mobility="FIXED" containedAssets="manager Laptop-manager AC_mngr" parentAsset="second_floor"/>
  <asset xsi:type="environment:Room" connections="meetingRoom1-hallway" name="meetingRoom1" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT24-hallway" name="office_T24" mobility="FIXED" containedAssets="desktop1_T24 desktop2_T24" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT23-hallway" name="office_T23" mobility="FIXED" containedAssets="desktop1_T23 desktop2_T23" parentAsset="second_floor"/>
  <asset xsi:type="environment:Office" connections="officeT25-hallway" name="office_T25" mobility="FIXED" containedAssets="desktop1_T25 desktop2_T25" parentAsset="second_floor"/>
  <asset xsi:type="environment:Room" connections="meetingRoom2-hallway" name="meetingRoom2" mobility="FIXED" parentAsset="second_floor"/>
  <asset xsi:type="environment:Employee" name="admin1" parentAsset="office_admin"/>
  <asset xsi:type="environment:Employee" name="admin2" parentAsset="office_admin"/>
  <asset xsi:type="environment:Employee" name="prof-BN" parentAsset="office_BN"/>
  <asset xsi:type="environment:Employee" name="manager" description="" parentAsset="office_Manager"/>
  <asset xsi:type="environment:SmartTV" name="smartTV-BN" parentAsset="office_BN"/>
  <asset xsi:type="environment:Visitor" name="visitor1" containedAssets="visitorLaptop" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:ComputingDevice" name="resourceCmptDevice" parentAsset="visitor1"/>
  <asset xsi:type="environment:Visitor" name="visitor2" parentAsset="hallway"/>
  <asset xsi:type="environment:Employee" name="researcher-T23" parentAsset="office_T23" role=""/>
  <asset xsi:type="environment:Employee" name="researcher-T24" parentAsset="office_T24" role=""/>
  <asset xsi:type="environment:Employee" name="researcher-T25" parentAsset="office_T25" role=""/>
  <asset xsi:type="environment:Employee" name="researcher-T26" parentAsset="office_T26" role=""/>
  <asset xsi:type="environment:Employee" name="researcher-T27" parentAsset="office_T27" role=""/>
  <asset xsi:type="environment:Employee" name="researcher-T28" parentAsset="office_T28" role=""/>
  <asset xsi:type="environment:Employee" name="researcher-T29" parentAsset="office_T29" role=""/>
  <asset xsi:type="environment:Employee" name="researcher-T30" parentAsset="office_T30" role=""/>
  <asset xsi:type="environment:CCTV" name="CCTV1" mobility="FIXED" parentAsset="hallway"/>
  <asset xsi:type="environment:AccessPoint" connections="iPConn_net-AP1" name="accessPoint1" mobility="FIXED" parentAsset="hallway"/>
  <asset xsi:type="environment:AccessPoint" connections="iPConn_net-AP2" name="accessPoint2" mobility="FIXED" parentAsset="hallway" model=""/>
  <asset xsi:type="environment:AccessPoint" connections="iPConn_net-AP3" name="accessPoint3" mobility="FIXED" parentAsset="hallway"/>
  <asset xsi:type="environment:Malware" name="malware" parentAsset="visitorLaptop"/>
  <asset xsi:type="environment:HVAC" connections="AC_admin-DN" name="AC_admin" description="" mobility="FIXED" parentAsset="office_admin"/>
  <asset xsi:type="environment:DigitalAsset" name="busNetworkData" parentAsset="busNetwork"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T23" name="SL_T23-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T24" name="SL_T24-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T25" name="SL_T25-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T26" name="SL_T26-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T27" name="SL_T27-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T28" name="SL_T28-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T28" name="SL_T28-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T29" name="SL_T29-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_T30" name="SL_T30-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_admin" name="SL_admin-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_BN" name="SL_BN-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_hallway" name="SL_hallway-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_MtgRm1" name="SL_MtgRm1-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_MtgRm2" name="SL_MtgRm2-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL_Mngr" name="SL_Mngr-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="FireAlarm1" name="FA-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="AC_mngr" name="HVAC-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Server1" name="Server-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Workstation1" name="Workstation-DN"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop1_T23" name="conn_1T23-IPNet" protocol="TCP/IP"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_T23" name="conn_2T23-IPNet"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_T24" name="conn_2T22-IPNet"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_T25" name="conn_2T25-IPNet"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_T26" name="conn_2T26-IPNet"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_T27" name="conn_2T27-IPNet" description=""/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop1_T28" name="conn_1T28-IPNet"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_T28" name="conn_2T28-IPNet"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop1_T29" name="conn_1T29-IPNet"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_T29" name="conn_2T29-IPNet"/>
  <connection xsi:type="environment:Walkway" asset1="stairsA" asset2="elevatorsArea" name="elevatorsArea-stairsA"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_admin" name="officeAdmin-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_BN" name="officeBN-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_Manager" name="officeManager-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T23" name="officeT23-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T24" name="officeT24-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T25" name="officeT25-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T26" name="officeT26-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T27" name="officeT27-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T28" name="officeT28-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T29" name="officeT29-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="office_T30" name="officeT30-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="meetingRoom1" name="meetingRoom1-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="meetingRoom2" name="meetingRoom2-hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="elevatorsArea" name="elevatorsArea-hallway">
    <constraints>CardOnly:WorkingHours[6:30-23:00]</constraints>
    <constraints>Closed:OffWorkingHours[23:00-6:30]</constraints>
  </connection>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="stairsB" name="stairsB-hallway">
    <constraints>CardOnly:WorkingHours[6:30-23:00]</constraints>
    <constraints>Closed:OffWorkingHours[23:00-6:30]</constraints>
  </connection>
  <connection xsi:type="environment:Walkway" asset1="hallway" name="informaticsLab_hallway2"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" name="openLab_hallway2"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="disabledToilet" name="disabledToilet_hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="mensToilet" name="mensToilet_hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="women'sToilet" name="womensToilet_hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="kitchen" name="kitchen_hallway"/>
  <connection xsi:type="environment:Walkway" asset1="hallway" asset2="kitchen" name="kitchen_hallway"/>
  <connection xsi:type="environment:IPConnection" asset1="accessPoint1" asset2="IPnetwork1" name="iPConn_net-AP1" protocol=""/>
  <connection xsi:type="environment:IPConnection" asset1="accessPoint2" asset2="IPnetwork1" name="iPConn_net-AP2"/>
  <connection xsi:type="environment:IPConnection" asset1="accessPoint3" asset2="IPnetwork1" name="iPConn_net-AP3" protocol=""/>
  <connection xsi:type="environment:DigitalConnection" asset1="AC_admin" asset2="busNetwork" name="AC_admin-DN"/>
  <action name="enter">
    <preconditions>ps1{con1} . (ac1 | act2{con2,con1}.act3.(act4 |act5{con2}.act6) | act7 | act8) || /con1 ps2{con1}.(ps3.(ps4 | ps5 | id) | id ) | ps6</preconditions>
    <postconditions>PhysicalStructure ps1 near PhysicalStructure ps2 contains Actor ac1</postconditions>
  </action>
  <action name="connect"/>
  <action name="disconnect"/>
  <action name="place"/>
  <action name="remove"/>
  <action name="talk"/>
</environment:EnvironmentDiagram>
