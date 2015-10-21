/**
 	#################### COPYRIGHT #####################################
    Copyright 2001, 2015, Rackspace Inc and/or its affiliates. All rights 
    reserved.
  	UNIX is a registered trademark of The Open Group.
  	This software and related documentation are provided under a license
  	agreement containing restrictions on use and disclosure and are
  	protected by intellectual property laws. Except as expressly permitted
  	in your license agreement or allowed by law, you may not use, copy,
  	reproduce, translate, broadcast, modify, license, transmit, distribute,
  	exhibit, perform, publish, or display any part, in any form, or by any
  	means. Reverse engineering, disassembly, or decompilation of this
  	software, unless required by law for interoperability, is prohibited.
  	The information contained herein is subject to change without notice
  	and is not warranted to be error-free. If you find any errors, please
  	report them to us in writing.

  	This software or hardware and documentation may provide access to or
  	information on content, products, and services from third parties.
  	Rackspace Inc, and its affiliates are not responsible for and
  	expressly disclaim all warranties of any kind with respect to
  	third-party content, products, and services. Rackspace Inc. and
  	its affiliates will not be responsible for any loss, costs, or damages
  	incurred due to your access to or use of third-party content, products,
  	or services.
 */
package com.rax.deployment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import atg.apache.xml.serialize.OutputFormat;
import atg.apache.xml.serialize.XMLSerializer;
import atg.deployment.common.DeploymentException;
import atg.deployment.server.DeploymentServer;
import atg.nucleus.ServiceException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.DynamoServlet;

/**
 * 
 * @author 
 *
 */
public class TopologyInfoDroplet extends DynamoServlet {
	
	private static final String ENCODING = "UTF-8";
	
	//output parameters
	private static final String PARAM_TOPOLOGY = "topology";
	private static final String PARAM_ERROR_MESSAGE = "errorMsg";
	
	//open parameters
	private static final String OPARAM_OUTPUT = "output";
	private static final String OPARAM_ERROR = "error";

	DeploymentServer mDeploymentServer;
	
	public DeploymentServer getDeploymentServer() {
		return mDeploymentServer;
	}
	public void setDeploymentServer(DeploymentServer pDeploymentServer) {
		this.mDeploymentServer = pDeploymentServer;
	}
	
	// -------------------------
	/**
	 * Checks that Nucleus components are  running, if its not, then throw
	 * a ServiceException.
	 * 
	 * @throws ServiceException
	*/
	public void doStartService() throws ServiceException {
		if (getDeploymentServer () == null) {
			throw new ServiceException("Deployment Server component is not running.");
		}
		
		super.doStartService();
	}

	@Override

	public void service(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			vlogDebug("TopologyInfoDroplet: service processing droplet " );
			getDeploymentServer().exportTopologyToXML(baos);
			String topologyConfig = new String(baos.toByteArray(),ENCODING);
			
			vlogDebug("TopologyInfoDroplet: topology: "  + topologyConfig );
			try {
				//String xmlOutput = format (topologyConfig);
				pRequest.setParameter(PARAM_TOPOLOGY, topologyConfig);
				
				vlogDebug("Setting output parameter");
				
				pRequest.serviceParameter(OPARAM_OUTPUT, pRequest, pResponse);
				
				
			} catch (Exception e) {
				vlogError (e,e.getMessage());
				
				pRequest.setParameter(PARAM_ERROR_MESSAGE, e.getMessage());
				pRequest.serviceParameter(OPARAM_ERROR, pRequest, pResponse);
			}
			
			
		} catch (DeploymentException e) {
			vlogError (e.getMessage(),e);
			
			pRequest.setParameter(PARAM_ERROR_MESSAGE, e.getMessage());
			pRequest.serviceParameter(OPARAM_ERROR, pRequest, pResponse);
		}
	}
	
	
	 public String format(String unformattedXml) {
        try {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(80);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	 }
	        
    private Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
}
