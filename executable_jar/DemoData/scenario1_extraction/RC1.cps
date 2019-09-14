<?xml version="1.0" encoding="UTF-8"?>
<environment:EnvironmentDiagram xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:environment="http://www.example.org/environment">
  <asset xsi:type="environment:Building" name="Research_Centre" description="" mobility="FIXED" containedAssets="fourteenth_floor">
    <type name="building"/>
  </asset>
  <asset xsi:type="environment:Floor" name="fourteenth_floor" description="" mobility="FIXED" containedAssets="visitingLab1 instructorsLab3 stairsA researchLab1 mensToilet lounge1 elevatorsArea instructorsLab7 stairsB kitchen instructorsLab5 openLab roomA hallway instructorsLab4 instructorsLab8 instructorsLab1 women'sToilet informaticsLab empiricalLab meetingRoom disabledToilet researchLab2 airConditioningRoom instructorsLab2 instructorsLab6" parentAsset="Research_Centre"/>
  <asset xsi:type="environment:SmartLight" connections="SL1-DN" name="SL1" mobility="FIXED" parentAsset="empiricalLab"/>
  <asset xsi:type="environment:SmartLight" connections="SL2-DN" name="SL2" mobility="FIXED" parentAsset="informaticsLab"/>
  <asset xsi:type="environment:SmartLight" connections="SL3-DN" name="SL3" mobility="FIXED" parentAsset="instructorsLab2"/>
  <asset xsi:type="environment:HVAC" connections="HVAC-DN" name="AirConditioning" mobility="FIXED" parentAsset="empiricalLab"/>
  <asset xsi:type="environment:FireAlarm" connections="FA-DN" name="FireAlarm1" parentAsset="hallway"/>
  <asset xsi:type="environment:Server" connections="Server-DN" name="Server1" parentAsset="empiricalLab"/>
  <asset xsi:type="environment:Workstation" connections="Workstation-DN" name="Workstation1" parentAsset="informaticsLab"/>
  <asset xsi:type="environment:BusNetwork" connections="HVAC-DN FA-DN SL2-DN SL1-DN Workstation-DN SL3-DN Server-DN" name="busNetwork" description="" mobility="FIXED"/>
  <asset xsi:type="environment:Visitor" name="Visitor1" containedAssets="Laptop1" parentAsset="elevatorsArea" role="Offender"/>
  <asset xsi:type="environment:Laptop" name="Laptop1" mobility="FIXED" containedAssets="SoftwareX" parentAsset="Visitor1"/>
  <asset xsi:type="environment:Malware" name="SoftwareX" parentAsset="Laptop1"/>
  <asset xsi:type="environment:Lab" connections="instructorsLab1_hallway" name="instructorsLab1" mobility="FIXED" containedAssets="desktop1_3 desktop1_2 desktop1_1" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Desktop" connections="d1_1" name="desktop1_1" description="" parentAsset="instructorsLab1"/>
  <asset xsi:type="environment:Desktop" connections="d1_2" name="desktop1_2" parentAsset="instructorsLab1"/>
  <asset xsi:type="environment:Desktop" name="desktop1_3" parentAsset="instructorsLab1"/>
  <asset xsi:type="environment:Desktop" connections="d2_1" name="desktop2_1" parentAsset="instructorsLab2"/>
  <asset xsi:type="environment:Desktop" name="desktop2_2" parentAsset="instructorsLab2"/>
  <asset xsi:type="environment:Desktop" connections="d3_1" name="desktop3_1" parentAsset="instructorsLab3" model=""/>
  <asset xsi:type="environment:Desktop" name="desktop3_2" description="" parentAsset="instructorsLab3"/>
  <asset xsi:type="environment:Desktop" connections="d4_1" name="desktop4_1" parentAsset="instructorsLab4" model=""/>
  <asset xsi:type="environment:Desktop" name="desktop4_2" parentAsset="instructorsLab4" model=""/>
  <asset xsi:type="environment:Desktop" connections="d5_1" name="desktop5_1" parentAsset="instructorsLab5"/>
  <asset xsi:type="environment:Desktop" connections="d5_2" name="desktop5_2" parentAsset="instructorsLab5"/>
  <asset xsi:type="environment:Desktop" connections="d6_1" name="desktop6_1" parentAsset="instructorsLab6"/>
  <asset xsi:type="environment:Desktop" connections="d6_2" name="desktop6_2" parentAsset="instructorsLab6"/>
  <asset xsi:type="environment:Desktop" connections="d7_1" name="desktop7_1" parentAsset="instructorsLab7"/>
  <asset xsi:type="environment:Desktop" name="desktop7_2" parentAsset="instructorsLab7"/>
  <asset xsi:type="environment:IPNetwork" connections="d5_1 d8_2 d6_1 d4_1 d1_2 d5_2 d6_2 d2_1 d3_1 d7_1 d1_1" name="IPnetwork1" mobility="FIXED" Protocol="TCP/IP" encryption="MACsec">
    <type name="Ethernet"/>
  </asset>
  <asset xsi:type="environment:Lab" connections="instructorsLab2_hallway" name="instructorsLab2" mobility="FIXED" containedAssets="desktop2_2 SL3 desktop2_1" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="instructorsLab3_hallway" name="instructorsLab3" description="" mobility="FIXED" containedAssets="desktop3_1 desktop3_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="instructorsLab4_hallway" name="instructorsLab4" mobility="FIXED" containedAssets="desktop4_2 desktop4_1" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="instructorsLab5_hallway" name="instructorsLab5" mobility="FIXED" containedAssets="desktop5_1 desktop5_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="instructorsLab6_hallway" name="instructorsLab6" mobility="FIXED" containedAssets="desktop6_2 desktop6_1" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="instructorsLab7_hallway" name="instructorsLab7" mobility="FIXED" containedAssets="desktop7_2 desktop7_1" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="openLab_hallway1 openLab_hallway2" name="openLab" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="empiricalLab_hallway2 empiricalLab_hallway1 empiricalLab_hallway4 empiricalLab_hallway3" name="empiricalLab" mobility="FIXED" containedAssets="AirConditioning server1 SL1" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="informaticsLab_hallway2 informaticsLab_hallway3 informaticsLab_hallway1 informaticsLab_hallway4" name="informaticsLab" mobility="FIXED" containedAssets="Workstation1 SL2 server2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Toilet" connections="mensToilet_hallway" name="mensToilet" description="" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Toilet" connections="womensToilet_hallway" name="women'sToilet" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Toilet" connections="disabledToilet_hallway" name="disabledToilet" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Kitchen" connections="kitchen_airConditioningRoom kitchen_hallway kitchen_hallway" name="kitchen" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Room" connections="kitchen_airConditioningRoom" name="airConditioningRoom" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="researchLab1_hallway" name="researchLab1" description="" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="researchLab2_hallway" name="researchLab2" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="visitingLab1_hallway" name="visitingLab1" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="visitingLab2_hallway" name="meetingRoom" mobility="FIXED" containedAssets="executiveLaptop" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Stairs" connections="roomA_stairsA" name="stairsA" description="" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Stairs" connections="stairsB_hallway" name="stairsB" description="" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lounge" connections="lounge_hallway2 lounge_hallway1" name="lounge1" mobility="FIXED" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:ElevatorsArea" connections="elevatorsArea_hallway" name="elevatorsArea" mobility="FIXED" containedAssets="elevator6 elevator3 elevator5 elevator1 Visitor1 elevator4 elevator2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Elevator" name="elevator1" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator2" parentAsset="elevatorsArea" model=""/>
  <asset xsi:type="environment:Elevator" name="elevator3" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator4" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator5" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator6" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" connections="roomA_emrgElv" name="emergencyElevator"/>
  <asset xsi:type="environment:Room" connections="roomA_hallway roomA_emrgElv roomA_stairsA" name="roomA" mobility="FIXED" containedAssets="cardReader1" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:CardReader" name="cardReader1" parentAsset="roomA"/>
  <asset xsi:type="environment:Hallway" connections="informaticsLab_hallway2 roomA_hallway lounge_hallway1 researchLab2_hallway instructorsLab7_hallway womensToilet_hallway mensToilet_hallway openLab_hallway1 empiricalLab_hallway2 visitingLab2_hallway disabledToilet_hallway instructorsLab2_hallway researchLab1_hallway instructorsLab3_hallway instructorsLab6_hallway visitingLab1_hallway elevatorsArea_hallway instructorsLab5_hallway lounge_hallway2 openLab_hallway2 instructorsLab1_hallway informaticsLab_hallway3 informaticsLab_hallway1 kitchen_hallway informaticsLab_hallway4 empiricalLab_hallway1 empiricalLab_hallway4 empiricalLab_hallway3 kitchen_hallway stairsB_hallway instructorsLab4_hallway" name="hallway" mobility="FIXED" containedAssets="FireAlarm1 SL-hallway" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="instructorsLab8" mobility="FIXED" containedAssets="desktop8_1 desktop8_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Desktop" connections="d7_1" name="desktop8_1" parentAsset="instructorsLab8"/>
  <asset xsi:type="environment:Desktop" connections="d8_2" name="desktop8_2" parentAsset="instructorsLab8"/>
  <asset xsi:type="environment:Server" name="server1" mobility="FIXED" parentAsset="empiricalLab"/>
  <asset xsi:type="environment:Server" name="server2" parentAsset="informaticsLab"/>
  <asset xsi:type="environment:Laptop" name="executiveLaptop" parentAsset="meetingRoom"/>
  <asset xsi:type="environment:SmartLight" name="SL-hallway" mobility="FIXED" parentAsset="hallway"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="FireAlarm1" name="FA-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="AirConditioning" name="HVAC-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Server1" name="Server-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL1" name="SL1-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL2" name="SL2-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL3" name="SL3-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Workstation1" name="Workstation-DN"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop1_1" name="d1_1" protocol="TCP/IP"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop1_2" name="d1_2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_1" name="d2_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop3_1" name="d3_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop4_1" name="d4_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop5_1" name="d5_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop5_2" name="d5_2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop6_1" name="d6_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop6_2" name="d6_2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop7_1" name="d7_1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="emergencyElevator" asset2="roomA" name="roomA_emrgElv"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="stairsA" asset2="roomA" name="roomA_stairsA"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab1" name="instructorsLab1_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab2" name="instructorsLab2_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab3" name="instructorsLab3_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab4" name="instructorsLab4_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab5" name="instructorsLab5_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab6" name="instructorsLab6_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab7" name="instructorsLab7_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="openLab" name="openLab_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="researchLab1" name="researchLab1_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="researchLab2" name="researchLab2_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="visitingLab1" name="visitingLab1_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="meetingRoom" name="visitingLab2_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="elevatorsArea" name="elevatorsArea_hallway">
    <constraints>Open:WorkingHours[8:30-19]</constraints>
    <constraints>CardOnly:WorkingHours[19-22]</constraints>
    <constraints>Closed:OffWorkingHours[22-8:30]</constraints>
  </connection>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="stairsB" name="stairsB_hallway">
    <constraints>Open:WorkingHours[8:30-19]</constraints>
    <constraints>Closed:OffWorkingHours[19-8:30]</constraints>
  </connection>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="roomA" name="roomA_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway2"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="openLab" name="openLab_hallway2"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway2"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway3"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway4"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway3"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway4"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="disabledToilet" name="disabledToilet_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="mensToilet" name="mensToilet_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="women'sToilet" name="womensToilet_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="kitchen" name="kitchen_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="airConditioningRoom" asset2="kitchen" name="kitchen_airConditioningRoom"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="kitchen" name="kitchen_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="lounge1" name="lounge_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="lounge1" name="lounge_hallway2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop8_2" name="d8_2"/>
</environment:EnvironmentDiagram>
