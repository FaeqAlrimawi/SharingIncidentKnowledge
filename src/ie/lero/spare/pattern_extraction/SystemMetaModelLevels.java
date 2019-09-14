package ie.lero.spare.pattern_extraction;

import environment.Asset;
import environment.BusConnection;
import environment.ComputingDevice;
import environment.CyberPhysicalSystemFactory;
import environment.DigitalAsset;
import environment.DigitalConnection;
import environment.Floor;
import environment.HVAC;
import environment.IPConnection;
import environment.Kitchen;
import environment.Lab;
import environment.PhysicalAsset;
import environment.PhysicalConnection;
import environment.PhysicalStructure;
import environment.Room;
import environment.Server;
import environment.SmartLight;
import environment.Status;

public class SystemMetaModelLevels {
	
	public  static void main(String [] args) {
		
		//ExtendedMetaData modelMetaData = new BasicExtendedMetaData(myResourceSet.getPackageRegistry());
		CyberPhysicalSystemFactory instance = CyberPhysicalSystemFactory.eINSTANCE;
		HVAC s = instance.createHVAC();
		s.setStatus(Status.OFF);
		//System.out.println(s.getStatus()+" "+s.isAbstractable());
		Floor f = instance.createFloor();
		Room r1 = instance.createRoom();
		Kitchen kit = instance.createKitchen();
		Lab lb = instance.createLab();
		Lab lb2 = instance.createLab();
		SmartLight sl = instance.createSmartLight();
		SmartLight sl2 = instance.createSmartLight();
		SmartLight sl3 = instance.createSmartLight();
		SmartLight sl4 = instance.createSmartLight();
		SmartLight sl5 = instance.createSmartLight();
		SmartLight sl6 = instance.createSmartLight();
		SmartLight sl7 = instance.createSmartLight();
		Asset abstractedAsset;
		ComputingDevice dev = instance.createComputingDevice();
		DigitalAsset dig1 = instance.createDigitalAsset();
		DigitalAsset dig2 = instance.createApplication();
		DigitalAsset dig3 = instance.createProcess();
		DigitalAsset dig4 = instance.createApplication();
		PhysicalAsset phys = instance.createPhysicalAsset();
		Server ser = instance.createServer();
		PhysicalStructure physStruct = instance.createPhysicalStructure();
		
		//connections
		DigitalConnection digCon = instance.createDigitalConnection();
		DigitalConnection digCon2 = instance.createDigitalConnection();
		PhysicalConnection physCon = instance.createPhysicalConnection();
		PhysicalConnection physCon2 = instance.createPhysicalConnection();
		BusConnection busCon = instance.createBusConnection();
		IPConnection ipCon = instance.createIPConnection();
		
		//ports
		/*Port p1 = instance.createPort();
		Port p2 = instance.createPort();
		Port p3 = instance.createPort();
		Port p4 = instance.createPort();
		Credential c1 = instance.createCredential();
		Credential c2 = instance.createCredential();
		Credential c3 = instance.createCredential();
		
		c1.setType(CredentialType.CARD);
		c2.setType(CredentialType.CARD);
		c3.setType(CredentialType.PINCODE);
		
		p1.setName("p1");
		p1.setAsset(dig2);
		p1.getCredential().add(c3);
		
		p2.setName("p1");
		p2.setAsset(sl2);
		p2.getCredential().add(c2);
		
		busCon.setAsset1(sl5);
		busCon.setAsset2(sl);
		busCon.setPort(p1);
		
		ipCon.setAsset1(sl3);
		ipCon.setAsset2(sl2);
		ipCon.setPort(p2);
		
		digCon.setAsset1(sl4);
		digCon.setAsset2(sl6);
		digCon2.setAsset1(sl7);
		digCon2.setAsset2(sl);
		
		//sl.getConnections().add(digCon2);
		sl.getConnections().add(digCon);
		
		//sl2.getConnections().add(digCon2);
		sl2.getConnections().add(digCon);
		sl2.getConnections().add(busCon);
		sl2.getConnections().add(digCon2);
		sl2.getConnections().add(ipCon);
		

		kit.setParentAsset(f);
		lb.setParentAsset(f);
		lb2.setParentAsset(f);
		kit.getContainedAssets().add(sl);
		//kit.getContainedAssets().add(sl6);
		kit.getContainedAssets().add(sl7);
		kit.getContainedAssets().add(sl2);
		kit.getContainedAssets().add(sl3);
		lb.getContainedAssets().add(sl4);
		lb2.getContainedAssets().add(sl6);
		//lb.getContainedAssets().add(sl5);
		//System.out.println(lb.isSimilarTo(lb2));
		//Room res =f.abstract_();//r1.abstract_();//f.abstract_();
		
		digCon.setBidirectional(false);
		digCon2.setBidirectional(true);
		
		System.out.println("****************************Max & Threshold*******************************");
		System.out.println("Max. Asset similarity value: " + Asset.SIMILARITY_MAXIMUM_VALUE);
		System.out.println("Threshold for asset similarity: " + Asset.SIMILARITY_THRESHOLD + " percentage: "+ ((double)Asset.SIMILARITY_THRESHOLD/(double)Asset.SIMILARITY_MAXIMUM_VALUE));
		System.out.println("Max. connection similarity value: "+ Connection.MAXIMUM_SIMILARITY_VALUE);
		System.out.println("threshold for connection similarity: "+ Connection.SIMILARITY_THRESHOLD_VALUE + " percentage: "+ ((double)Connection.SIMILARITY_THRESHOLD_VALUE/(double)Connection.MAXIMUM_SIMILARITY_VALUE));
		System.out.println("*************************************************************************\n\n");
		
		
		//int simi = ipCon.similarTo(busCon);
		//System.out.println("Connection similarity: "+ simi + "\npercentage: " + ((double)simi/(double)Connection.MAXIMUM_SIMILARITY_VALUE));
	
		//connections similarity
		Asset ast1 = dev;
		Asset ast2 = sl;
		double type = ast1.compareType(ast2);
		double parent = ast1.compareParentAsset(ast2);
		double child = ast1.compareContainedAssets(ast2);
		double conns = ast1.compareConnections(ast2);
		double total = ast1.similarTo(ast2);
		System.out.println("type similarity= "+  type + " percentage:" + ((double)type/(double)Asset.EXACT_TYPE));
		System.out.println("parent asset similarity= "+ parent + " percentage: " + ((double)parent/(double)Asset.COMMON_PARENT));
		System.out.println("contained assets similarity= "+ child + " percentage:" + ((double)child/(double)Asset.CONTAINEDASSETS_EXACT));
		System.out.println("connection similarity= "+ conns +" percentage:"+ ((double)conns/(double)Connection.MAXIMUM_SIMILARITY_VALUE));
		System.out.println("total similarity= " + total +" percentage:" + ((double)total/(double)Asset.SIMILARITY_MAXIMUM_VALUE));	
		//System.out.println("port similarity: "+busCon.comparePort(ipCon));
		
		Asset ast = physStruct.abstractAsset();
		System.out.println("abstract asset type:" + ast.getName());
		
		dig1.getContainedAssets().add(dig2);
		//dig1.getContainedAssets().add(dig3);
		dig1.getContainedAssets().add(dig4);
		dig2.getContainedAssets().add(dig3);
		for(Asset assst : dig1.getContainedAssets()) {
			System.out.println(assst.getName()+" "+assst.getClass());
		}
		
		DigitalAsset aset2 = (DigitalAsset)dig1.abstractAsset();
	
		for(Asset assst : aset2.getContainedAssets()) {
			System.out.println(aset2.getName()+" "+assst.getName()+" "+assst.getClass());
		}
		
		for(Asset assst : dig2.getContainedAssets()) {
			System.out.println(dig2.getName()+" "+assst.getName()+" "+assst.getClass());
		}
		
		for(Asset assst : ((DigitalAsset)dig2.getAbstractedAsset()).getContainedAssets()) {
			System.out.println(dig2.getName()+" "+assst.getName()+" "+assst.getClass());
		}
		
		Connection con = busCon.abstractConnection();
		System.out.println(con.getName()+" "+con.getAsset1() +" "+con.getAsset2());
		*/
		/*int length = 10;
		Asset [] asts = new Asset[length];
		Asset [] astsAbstracted = new Asset[length];
		
		for(int i=0;i<asts.length;i++) {
			asts[i] = instance.createSmartLight();
		}
		
		for(int i=0;i<asts.length;i++) {
			astsAbstracted[i] = asts[i].abstractAsset();
		}
		
		
		for(int i=0;i<asts.length;i++) {
			System.out.println("abstract asset ["+i+"]"+": class=" + astsAbstracted[i].getClass().getName() + " name="+astsAbstracted[i].getName());
		}*/
		
		//Asset fAbstracted = f.abstractAsset();
		//Asset slAbstracted = sl.abstractAsset();
		//abstractedAsset = sl.abstractAsset();
		
		
		//System.out.println("connection similarity: "+digCon.isSimilarTo(digCon2));
		/*if(res != null) {
			System.out.println(res.getName());
		} else {
			System.out.println("room is null");
		}*/
		
	/*	if(abstractedAsset != null) {
		System.out.println(abstractedAsset.getClass());
	} else {
		System.out.println("abstracted asset is null");
	}*/
		
		
	}

}
