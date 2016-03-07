package si.fri.mag.gasperin.cep.utils;

import java.math.BigInteger;
import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.Operation;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.Resource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.core.service.CseService;

public class RequestSender {
	
	public static CseService CSE;
	
	/**
	 * Private constructor to avoid creation of this object
	 */
	private RequestSender(){}
	
	public static ResponsePrimitive createResource(String targetId, String name, Resource resource, int resourceType){
		
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(targetId);
		request.setResourceType(BigInteger.valueOf(resourceType));
		request.setRequestContentType(MimeMediaType.OBJ);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setContent(resource);
		request.setName(name);
		request.setOperation(Operation.CREATE);
		
		return CSE.doRequest(request);
		
	}
	
	
	public static ResponsePrimitive createAE(AE resource, String name){
		return createResource("/" + Constants.CSE_ID, name, resource, ResourceType.AE);
	}
	
	public static ResponsePrimitive createContainer(String targetId, String name, Container resource){
		return createResource(targetId, name, resource, ResourceType.CONTAINER);
	}
	
	public static ResponsePrimitive createContentInstance(String targetId, String name, ContentInstance resource){
				
		return createResource(targetId, name, resource, ResourceType.CONTENT_INSTANCE);
		
	}
	
	public static ResponsePrimitive createContentInstance(String targetId, ContentInstance resource){
		return createContentInstance(targetId, null, resource);
	}
	
	public static ResponsePrimitive sendDataToDevice(String targetId, ContentInstance resource){
		return createContentInstance(targetId, null, resource);
	}
	

	public static ResponsePrimitive getRequest(String targetId){
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(targetId);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setOperation(Operation.RETRIEVE);
		request.setRequestContentType(MimeMediaType.OBJ);
		return CSE.doRequest(request);
	}
	
	public static ResponsePrimitive deleteResource(String targetId, String name, Resource resource, int resourceType){
		
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(targetId);
		request.setResourceType(BigInteger.valueOf(resourceType));
		request.setRequestContentType(MimeMediaType.OBJ);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setContent(resource);
		request.setName(name);
		request.setOperation(Operation.DELETE);
		
		return CSE.doRequest(request);
		
	}
	
	public static ResponsePrimitive deleteContainer(String targetId){
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(targetId);
		request.setOperation(Operation.DELETE);
		
		return CSE.doRequest(request);
	}
	
}