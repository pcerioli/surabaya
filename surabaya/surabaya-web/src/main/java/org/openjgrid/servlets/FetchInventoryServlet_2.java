/**
 *  Surabaya - a replacement http server for the OpenSimulator
 *  Copyright (C) 2012 Akira Sonoda
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openjgrid.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openjgrid.datatypes.inventory.InventoryException;
import org.openjgrid.datatypes.inventory.InventoryItemBase;
import org.openjgrid.datatypes.llsd.LLSD;
import org.openjgrid.datatypes.llsd.LLSDFetchInventory;
import org.openjgrid.datatypes.llsd.LLSDInventoryItem;
import org.openjgrid.services.inventory.InventoryService_2;
import org.openjgrid.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FetchInventory Servlet
 * 
 * This Servlet will handle the FetchInventory2 OpenSim CAPS
 * 
 * In order to provide some security all CAPS URL contain a random UUID for each
 * Capability. The UUID of an incoming request will be checked against the UUID 
 * stored in the logged in Agent which itself is managed in the AgentManagementService
 * 
 * If the given UUID fits the corresponding CAPS funcion is called. This security however
 * is only secure as long as the Transport is encrypted
 * 
 * TODO implement encrypted Transport
 * 
 * Author: Akira Sonoda
 */
@WebServlet(name = "FetchInventoryServlet_2", urlPatterns = { "/fetchinventory2/*" })
public class FetchInventoryServlet_2 extends HttpServlet {
 	private static final long serialVersionUID = -1815134758746814053L;

 	private static final Logger log = LoggerFactory.getLogger(InventoryDescendentsServlet_2.class);


	@EJB
	private InventoryService_2 inventoryService;

	private void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			log.info("FetchInventoryServlet_2");
			OutputStream out = response.getOutputStream();
			HttpClient httpclient = new DefaultHttpClient();

			assert(Util.dumpHttpRequest(request));

			String inventoryServerName = null;
			String inventoryServerPort = null;
			
			String url = request.getRequestURI();
			Pattern p = Pattern.compile("^/fetchinventory2/(.*)/(.*)$");
			Matcher m = p.matcher(url);
			if (m.find()) {
				inventoryServerName = m.group(1);
				inventoryServerPort = m.group(2);
			}
			
			String inventoryServerURL = "http://" + inventoryServerName + ":" + inventoryServerPort;
			
			response.setContentType(request.getContentType());
			String reply = fetchInventory(request, httpclient, inventoryServerURL);
			StringEntity entity = new StringEntity(reply);
			entity.writeTo(out);
			out.close();

		} catch (Exception ex) {
			log.debug("Exception {} occurred", ex.getClass().toString());
		}
	}

	@SuppressWarnings("unchecked")
	private String fetchInventory(HttpServletRequest request, HttpClient httpclient, String inventoryServerURL ) 
		throws IOException, XMLStreamException, InventoryException {
		
		log.debug("fetchInventory2() called");
		String requestString = Util.requestContent2String(request);
		log.debug("Content: {}", requestString);

		HashMap<String, Object> llsdRequestMap = null;
		try {
			llsdRequestMap = (HashMap<String, Object>) LLSD.llsdDeserialize(requestString);
		} catch (Exception ex) {
			log.error("Fetch error: {} {}" + ex.getMessage(), ex.getStackTrace());
			log.error("Request {}: ", request);
		}
		ArrayList<Object> itemsrequested = (ArrayList<Object>) llsdRequestMap.get("items");

        LLSDFetchInventory llsdReply = new LLSDFetchInventory();
		
		Iterator<Object> itemsIterator = itemsrequested.iterator();
		while(itemsIterator.hasNext()) {
			Map<String, Object> itemMap = (Map<String, Object>) itemsIterator.next();
			UUID itemID = (UUID) itemMap.get("item_id");

			InventoryItemBase item = inventoryService.getItem(new InventoryItemBase(itemID), inventoryServerURL);
			if( item != null ) {
                // We don't know the agent that this request belongs to so we'll use the agent id of the item
                // which will be the same for all items.
                llsdReply.agent_id = item.getOwnerId();
				llsdReply.items.add(new LLSDInventoryItem(item));
			}
		}

		String response = new String();
		try {
			response = LLSD.LLSDSerialize(llsdReply);
		} catch (Exception ex) {
			log.error("LLSD serialize error: {} {}" + ex.getMessage(),ex.getStackTrace());
			log.error("Request {}: ", request);
		}

		log.debug("outgoingresponse: {}", response);

		return (response);
	}

	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

}
