package com.infometry.tickets;

import javax.ws.rs.core.MediaType;
import com.infometry.hubspot.HubspotConn;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class DeleteTicket {
	public static void main(String[] args) throws Exception {
		
		String token = HubspotConn.refreshAccessToken();
		String uri = "https://api.hubapi.com/crm/v3/objects/tickets/49925146";
		Client client = Client.create();
		
		ClientResponse response = client.resource(uri).header("Authorization", "Bearer "+token)
				.type(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
		
		if(response.getStatus() == 204) {
			System.out.println("deleted");
		}else {
			System.out.println(response);
		}
	}
}
