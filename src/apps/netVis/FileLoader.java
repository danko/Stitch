package apps.netVis;

import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

public class FileLoader {

	static DocumentBuilder db = null;

	public static void load(String fn) throws Exception {
		DocumentBuilder db = new DocumentBuilderFactoryImpl().newDocumentBuilder();
		Document doc = db.parse(fn);

		NodeList nl = doc.getElementsByTagName("payload:DataType");
		System.out.println(nl.item(0).getTextContent());
		
		nl = doc.getChildNodes();
		for(int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i);
			if(n.getNodeName() == "payload:Payload") {
				
				Node e = doc.getElementsByTagName("payload:DataType").item(0);
				System.out.println(e.getTextContent());
			}
			System.out.println();
			//System.out.println(n.getAttributes().getNamedItem("hostName").getTextContent());
		}
	}

}
