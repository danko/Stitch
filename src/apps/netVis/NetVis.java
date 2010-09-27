package apps.netVis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

import apps.App;
import arch.model.DataFormat;
import arch.model.DataSource;
import arch.model.DhcpEnzyme;
import arch.model.DhcpLease;
import arch.model.Domain;
import arch.model.Entity;
import arch.model.ExecutableBinaryImage;
import arch.model.GatewayEnzyme;
import arch.model.IPAddress;
import arch.model.InvalidKeyException;
import arch.model.PhysicalHost;
import arch.model.ProcessInstance;
import arch.model.Project;
import arch.model.Provenance;
import arch.model.Server;
import arch.model.Stitch;

public class NetVis extends App {

	class IntegerByReference { int val; }

	public NetVis(Stitch stitchModel) {
		super(stitchModel);
		
		manifestDataFormat(DataFormat.Tag.EXE_TABLE);
		manifestDataFormat(DataFormat.Tag.METADATA);
		manifestDataFormat(DataFormat.Tag.ARP_CONFIG);
		manifestDataFormat(DataFormat.Tag.IP_CONFIG);
		manifestDataFormat(DataFormat.Tag.NETMAP);
		manifestDataFormat(DataFormat.Tag.PROCESS_LIST);
	}

	public static Date parseTimeStamp(String s) throws InvalidTimeFormat {
		if(s == null) throw new InvalidTimeFormat("null");
		int a, b = 0;

		a = b; b = s.indexOf('-', a); if(b == -1) throw new InvalidTimeFormat(s);
		String year = s.substring(a, b);
		a = b; b = s.indexOf('-', a+1); if(b == -1) throw new InvalidTimeFormat(s);
		String month = s.substring(a+1, b);
		a = b; b = s.indexOf('T', a+1); if(b == -1) throw new InvalidTimeFormat(s);
		String day = s.substring(a+1, b);

		a = b; b = s.indexOf(':', a+1); if(b == -1) throw new InvalidTimeFormat(s);
		String hour = s.substring(a+1, b);
		a = b; b = s.indexOf(':', a+1); if(b == -1) throw new InvalidTimeFormat(s);
		String minute = s.substring(a+1, b);
		a = b; b = s.length();
		String second = s.substring(a+1, a+3);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.valueOf(year));
		cal.set(Calendar.MONTH, Integer.valueOf(month));
		cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
		cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
		cal.set(Calendar.MINUTE, Integer.valueOf(minute));
		cal.set(Calendar.SECOND, Integer.valueOf(second));
		
		return cal.getTime();
	}
	
	public static String readNextToken(String line, IntegerByReference pos) {
		if(pos.val >= line.length()) return null;
		int a = pos.val, b = pos.val;
		if(line.charAt(pos.val) == '\"') {
			++a;
			b = line.indexOf("\"", a);
		} else {
			b = line.indexOf(",", b);
			if(b == -1) b = line.length();
		}
		pos.val = line.indexOf(",", b) + 1;
		if(pos.val == 0) pos.val = line.length();
		return line.substring(a, b);
	}

	private DataFormat manifestDataFormat(DataFormat.Tag t) {
		Entity entity = stitchModel.getEntity(t.name());
		if(entity == null) {
			try { entity = new DataFormat(this, t.name()); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(DataFormat.class.isInstance(entity));
		return (DataFormat) entity;
	}
	
	private DataSource manifestDataSource(File f) {
		String path = f.getPath();
		if(f.getName().startsWith("cdrf")) path = f.getName(); //special case copilotIDs
		DataSource ds = manifestDataSource(path);
		ds.setFile(f);
		return ds;
	}
	
	private DataSource manifestDataSource(String path) {
		Entity entity = stitchModel.getEntity(path);
		if(entity == null) {
			try { entity = new DataSource(path); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(DataSource.class.isInstance(entity));
		return (DataSource) entity;
	}
	
	private PhysicalHost manifestPhysicalHost(String mac) {
		Entity entity = stitchModel.getEntity(mac);
		if(entity == null) {
			try { entity = new PhysicalHost(mac); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(PhysicalHost.class.isInstance(entity));
		return (PhysicalHost) entity;
	}
	
	private IPAddress manifestIPAddress(String ip) {
		Entity entity = stitchModel.getEntity(ip);
		if(entity == null) {
			try { entity = new IPAddress(ip); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(IPAddress.class.isInstance(entity));
		return (IPAddress) entity;
	}
	
	private ProcessInstance manifestProcessInstance(String URI, String pid) {
		String key = ProcessInstance.genKey(URI, Integer.valueOf(pid));
		Entity entity = stitchModel.getEntity(key);
		if(entity == null) {
			try { entity = new ProcessInstance(key); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(ProcessInstance.class.isInstance(entity));
		return (ProcessInstance) entity;
	}
	
	private Project manifestProject(String name) {
		Entity entity = stitchModel.getEntity(name);
		if(entity == null) {
			try { entity = new Project(name); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(Project.class.isInstance(entity));
		return (Project) entity;
	}

	private ExecutableBinaryImage manifestExecutableBinaryImage(String label) {
		Entity entity = stitchModel.getEntity(label);
		if(entity == null) {
			try { entity = new ExecutableBinaryImage(label); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(ExecutableBinaryImage.class.isInstance(entity));
		return (ExecutableBinaryImage) entity;
	}
	
	private Domain manifestDomain(String name) {
		Entity entity = stitchModel.getEntity(name);
		if(entity == null) {
			try { entity = new Domain(name); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(Domain.class.isInstance(entity));
		return (Domain) entity;
	}

	private Server manifestServer(String name) {
		Entity entity = stitchModel.getEntity(name);
		if(entity == null) {
			try { entity = new Server(name); }
			catch(InvalidKeyException e) { e.printStackTrace(); return null; }
			stitchModel.addEntity(entity);
		}
		assert(Server.class.isInstance(entity));
		return (Server) entity;
	}

	@Override
	public void scanFile(File f) { // Attempt to identify data format of f
		if(f.getName().endsWith(".csv")) {
			Entity.associateMutually(manifestDataSource(f), manifestDataFormat(DataFormat.Tag.METADATA));
			return;
		}
		
		if(f.getName().endsWith(".pld")) {
			Entity.associateMutually(manifestDataSource(f), manifestDataFormat(DataFormat.Tag.EXE_TABLE));
			return;
		}
		
		try {
			if(docBuilder == null) docBuilder = new DocumentBuilderFactoryImpl().newDocumentBuilder();
			Document doc = docBuilder.parse(f);
	
			NodeList nl = doc.getElementsByTagName("payload:DataType");
			String type = nl.item(0).getTextContent();
	
			     if(type.equals("IP Configuration"))	Entity.associateMutually(manifestDataSource(f), manifestDataFormat(DataFormat.Tag.IP_CONFIG));
			else if(type.equals("ARP Configuration"))	Entity.associateMutually(manifestDataSource(f), manifestDataFormat(DataFormat.Tag.ARP_CONFIG));
			else if(type.equals("Network Mapping"))		Entity.associateMutually(manifestDataSource(f), manifestDataFormat(DataFormat.Tag.NETMAP));
			else if(type.equals("Process List"))		Entity.associateMutually(manifestDataSource(f), manifestDataFormat(DataFormat.Tag.PROCESS_LIST));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	@Override
	public void loadFile(File f) {
		if(f.getName().endsWith(".csv")) {
			load_metadata(f);
			return;
		}
		
		if(f.getName().endsWith(".pld")) {
			load_processLabels(f);
			return;
		}
		
		try {
			if(docBuilder == null) docBuilder = new DocumentBuilderFactoryImpl().newDocumentBuilder();
			Document doc = docBuilder.parse(f);
	
			NodeList nl = doc.getElementsByTagName("payload:DataType");
			String type = nl.item(0).getTextContent();
			
			     if(type.equals("IP Configuration"))	load_IPConfig(f, doc);
			else if(type.equals("ARP Configuration"))	load_ARPConfig(f, doc);
			else if(type.equals("Network Mapping"))		load_NetMap(f, doc);
			else if(type.equals("Process List"))		load_ProcList(f, doc);
		}
		catch(Exception e) { e.printStackTrace(); }
	}

	private void load_metadata(File f) {
		try {
			stitchModel.pushTimeContext(new Date());

			ArrayList<String> columns = new ArrayList<String>();
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				IntegerByReference pos = new IntegerByReference();

				{ //Read Header Row
					pos.val = 0;
					line = in.readLine();
					if(line == null) return; //empty file
					String token;
					do {
						token = readNextToken(line, pos);
						if(token != null) columns.add(token);
					} while(token != null);
				}
				
				do {
					pos.val = 0;
					line = in.readLine();
					if(line != null) {
						String token;
						int column = 0;
						Metadata row = new Metadata();
						do {
							token = readNextToken(line, pos);
							try {
								java.lang.reflect.Field dataField = Metadata.mapping.get(columns.get(column));
								if(dataField != null) dataField.set(row, token);
							} catch(Exception e) {}
							++column;
						} while(token != null);

						//Populate Project Object
						try { stitchModel.setTimeContext(parseTimeStamp(row.acquisition)); }
						catch (InvalidTimeFormat exc) { exc.printStackTrace(); }
						
						Project proj = manifestProject(row.project);
						proj.addProvenance(new Provenance(manifestDataSource(f)));
						proj.addCopilotID(stitchModel, row.copilotID);
						DataSource ds = manifestDataSource(row.copilotID + ".dat");
						Entity.associateMutually(ds, proj); // sets the owner of the dataSource to this project

						// Associate project with any entities
						// which have provenance from a file it owns 
						for(Entity e : stitchModel.getEntities()) { //find associations
							if( e.hasProvenance(ds) )
								Entity.associateMutually(ds.getOwner(), e);
						}
						
						proj.init(); //base construction complete
					}
				} while(line != null);
			} catch (IOException e) { e.printStackTrace(); }

		} finally {
			stitchModel.popTimeContext();
		}
	}

	private void load_ProcList(File f, Document doc) {
		NodeList timeInfo = doc.getElementsByTagName("payload:TargetTime");
		String timeStamp = timeInfo.item(0).getTextContent();

		try {
			Date time = parseTimeStamp(timeStamp);
			stitchModel.pushTimeContext(time);
			
			Node procs = doc.getElementsByTagName("process:ProcessList").item(0);
			for(Node p = procs.getFirstChild(); p != null; p = p.getNextSibling()) {
				Element e = (Element) p;

				String executable = p.getTextContent();
				ExecutableBinaryImage ebi = manifestExecutableBinaryImage(executable);

				String pid = e.getAttribute("pid");
				ProcessInstance proc = manifestProcessInstance(doc.getBaseURI(), pid);
				proc.addProvenance(new Provenance(manifestDataSource(f)));
				Date creationTime = parseTimeStamp(e.getAttribute("creationTime"));
				proc.setCreationTime(creationTime);
				
				Entity.associateMutually(ebi, proc);
				
				//attempt to associate with a project
				Project owner = manifestDataSource(f).getOwner();
				if(owner != null) owner.associate(proc);
				
				proc.init(); //base construction complete
			}

		}
		catch(InvalidTimeFormat exc) { System.err.println("Invalid Time Format: \"" + exc.sample + "\""); }
		finally {
			stitchModel.popTimeContext();
		}
	}

	private void load_NetMap(File f, Document doc) {
		NodeList timeInfo = doc.getElementsByTagName("payload:TargetTime");
		String timeStamp = timeInfo.item(0).getTextContent();

		Provenance provenance = new Provenance(manifestDataSource(f));
		try {
			Date time = parseTimeStamp(timeStamp);
			stitchModel.pushTimeContext(time);
		
			NodeList netmaps = doc.getElementsByTagName("netmap:Netmap");
			for(int n = 0; n < netmaps.getLength(); ++n) {
				Node netmap = netmaps.item(n);
				for(Node resource = netmap.getFirstChild(); resource != null; resource = resource.getNextSibling()) {
					if(resource.getNodeName().equals("netmap:GenericResource")) {
						if(((Element) resource).getAttribute("name").equals("Microsoft Windows Network")) {
							for(Node domain = resource.getFirstChild(); domain != null; domain = domain.getNextSibling()) {
								if(!domain.getNodeName().equals("netmap:Domain")) continue;
								String domainName = ((Element) domain).getAttribute("name");
								Domain domainEntity = manifestDomain(domainName);
								domainEntity.addProvenance(provenance);
								for(Node server = domain.getFirstChild(); server != null; server = server.getNextSibling()) {
									if(!server.getNodeName().equals("netmap:Server")) continue;
									Element e = (Element) server;
									Server serverEntity = manifestServer(e.getAttribute("name"));
									serverEntity.setLocalName(e.getAttribute("localName"));

									for(Node detail = server.getFirstChild(); detail != null; detail = detail.getNextSibling()) {
										
										if(detail.getNodeName().equals("netmap:IpAddress")) {
											for(Node ipAddress = detail.getFirstChild(); ipAddress != null; ipAddress = ipAddress.getNextSibling()) {
												if(ipAddress.getNodeName().equals("common:IPv4Address")) {
													String ip = ipAddress.getTextContent();
													IPAddress ipEntity = manifestIPAddress(ip);
													Entity.associateMutually(serverEntity, ipEntity);
													ipEntity.addProvenance(provenance);
													ipEntity.init();
												}
											}
										}
										
									}
									
									serverEntity.addProvenance(provenance);
									serverEntity.init();
								
									domainEntity.associate(serverEntity);
								}
								domainEntity.init();
							}
						}
					}
				}
			}
		}
		catch(InvalidTimeFormat exc) { System.err.println("Invalid Time Format: \"" + exc.sample + "\""); }
		finally {
			stitchModel.popTimeContext();
		}
	}

	private void load_ARPConfig(File f, Document doc) throws Exception {
		NodeList timeInfo = doc.getElementsByTagName("payload:TargetTime");
		String timeStamp = timeInfo.item(0).getTextContent();
		Date time = parseTimeStamp(timeStamp);

		Provenance provenance = new Provenance(manifestDataSource(f));
		try {
			stitchModel.pushTimeContext(time);
		
			NodeList arp = doc.getElementsByTagName("arp:Arp");
			for(int n = 0; n < arp.getLength(); ++n) {
				NodeList arpEntries = arp.item(n).getChildNodes();
				for(int e = 0; e < arpEntries.getLength(); ++e) {
					Node entry = arpEntries.item(e);
					
					try {
						PhysicalHost host = null;
						for(Node item = entry.getFirstChild(); item != null; item = item.getNextSibling()) {
							if(item.getNodeName().equals("arp:PhysicalAddress")) {
								String mac = item.getTextContent();
								if(mac.equals("")) {
									System.out.println("Empty MAC-Address field in " + doc.getBaseURI());
									throw new InvalidKeyException();
								}
								host = manifestPhysicalHost(mac);
								host.addProvenance(provenance);
								break;
							}
						}
						assert(host != null);
	
						for(Node item = entry.getFirstChild(); item != null; item = item.getNextSibling()) {
							
							if(item.getNodeName().equals("arp:IpAddress")) {
								String ip = item.getTextContent();
								IPAddress ipEntity = manifestIPAddress(ip);
								ipEntity.addProvenance(provenance);
								Entity.associateMutually(host, ipEntity);
								ipEntity.init();
							}
	
						}

						host.init();
					} catch(InvalidKeyException ike) {}
					
				}
			}

		} finally {
			stitchModel.popTimeContext();
		}
	}

	private void load_IPConfig(File f, Document doc) {
		NodeList timeInfo = doc.getElementsByTagName("payload:TargetTime");
		String timeStamp = timeInfo.item(0).getTextContent();

		Provenance provenance = new Provenance(manifestDataSource(f));
		try {
			Date time = parseTimeStamp(timeStamp);
			stitchModel.pushTimeContext(time);

			NodeList ipConfigs = doc.getElementsByTagName("ip:IpConfig");
			for(int i = 0; i < ipConfigs.getLength(); ++i) {
				Node ipConfig = ipConfigs.item(i);
				
				String MAC = null, IPv4Address = null, subnetMask = null, gatewayIP = null,
					dhcpIP = null, leaseObtained = null, leaseExpires = null;
				
				for(Node item = ipConfig.getFirstChild(); item != null; item = item.getNextSibling()) {
					
					if(item.getNodeName().equals("ip:Adapter")) {
						for(Node adapterItem = item.getFirstChild(); adapterItem != null; adapterItem = adapterItem.getNextSibling()) {
							if(adapterItem.getNodeName().equals("ip:IpAddress")) {
								for(Node addressItem = adapterItem.getFirstChild(); addressItem != null; addressItem = addressItem.getNextSibling()) {
									if(addressItem.getNodeName().equals("common:IPv4Address"))
										IPv4Address = addressItem.getTextContent();
									if(addressItem.getNodeName().equals("common:IPv4SubnetMask"))
										subnetMask = addressItem.getTextContent();
								}
							}
							if(adapterItem.getNodeName().equals("ip:PhysicalAddress"))
								MAC = adapterItem.getTextContent();
							if(adapterItem.getNodeName().equals("ip:Gateway")) {
								for(Node gatewayItem = adapterItem.getFirstChild(); gatewayItem != null; gatewayItem = gatewayItem.getNextSibling()) {
									if(gatewayItem.getNodeName().equals("common:IPv4Address"))
										gatewayIP = gatewayItem.getTextContent();
								}
							}
							if(adapterItem.getNodeName().equals("ip:Dhcp")) {
								Element dhcp = (Element) adapterItem;
								leaseObtained = dhcp.getAttribute("leaseExpires"); //reversed in xml files?
								leaseExpires  = dhcp.getAttribute("leaseObtained" );
								for(Node dhcpItem = adapterItem.getFirstChild(); dhcpItem != null; dhcpItem = dhcpItem.getNextSibling()) {
									if(dhcpItem.getNodeName().equals("ip:Server")) {
										for(Node serverItem = dhcpItem.getFirstChild(); serverItem != null; serverItem = serverItem.getNextSibling()) {
											if(serverItem.getNodeName().equals("common:IPv4Address"))
												dhcpIP = serverItem.getTextContent();				
										}
									}
								}
							}
						}
					}
					
				}

				IPAddress ip = null;
				if(IPv4Address != null) {
					ip = manifestIPAddress(IPv4Address);
					ip.addProvenance(provenance);
					if(subnetMask != null) ip.setSubnetMask(subnetMask);
					ip.init();
				}
				
				IPAddress gateway = null;
				if(gatewayIP != null) {
					gateway = manifestIPAddress(gatewayIP);
					gateway.addProvenance(provenance);
					gateway.init();
				}
				
				IPAddress dhcp = null;
				if(dhcpIP != null) {
					dhcp = manifestIPAddress(dhcpIP);
					dhcp.addProvenance(provenance);
					dhcp.init();
				}
				
				PhysicalHost host = null;
				if(MAC != null) {
					host = manifestPhysicalHost(MAC);
					host.addProvenance(provenance);
					
					if(ip != null) Entity.associateMutually(host, ip);
					if(gateway != null) {
						host.associate(GatewayEnzyme.wrap(gateway));
						gateway.associate(host);
					}
					if(dhcp != null){
						host.associate(DhcpEnzyme.wrap(dhcp));
						dhcp.associate(host);
					}
					if(leaseObtained != null || leaseExpires != null) {
						try {
							host.associate(new DhcpLease(dhcp, parseTimeStamp(leaseObtained), parseTimeStamp(leaseExpires)));
						} catch(InvalidTimeFormat ecx) {}
					}

					DataSource ds = manifestDataSource(f);
					if(ds.getOwner() != null)
						Entity.associateMutually(ds.getOwner(), host); //project<->physicalHost
					
					host.init();
				}

			}

		}
		catch(InvalidTimeFormat exc) { System.err.println("Invalid Time Format: \"" + exc.sample + "\""); }
		finally {
			stitchModel.popTimeContext();
		}
	}

	private void load_processLabels(File f) {
		try {
			FileReader reader = new FileReader(f);
			BufferedReader in = new BufferedReader(reader);
			String line;
			
			Provenance provenance = new Provenance(manifestDataSource(f));
			do {
				line = in.readLine();
				if(line == null) break;
				if(line.length() > 2) {
					String file = new String(), label = new String();
					boolean colon = false;
					byte[] bytes = line.getBytes();
					for(int i = 1; i < bytes.length; i += 2) {
						if(bytes[i-1] == 255) ++i;
						if(bytes[i] == ':') { i += 14; colon = true; }

						if(!colon) file += (char) bytes[i];
						else label += (char) bytes[i];
					}
					if(colon) { //if full entry pair parsed
						ExecutableBinaryImage ebi = manifestExecutableBinaryImage(file);
						ebi.setLabel(label);
						ebi.addProvenance(provenance);
						ebi.init();
					}
				}
			} while(true);
			
		} catch(IOException e) {}
	}
	
}
