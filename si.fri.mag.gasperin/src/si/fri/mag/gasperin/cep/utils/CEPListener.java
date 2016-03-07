package si.fri.mag.gasperin.cep.utils;

import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.io.ObixEncoder;
import org.eclipse.om2m.commons.resource.ContentInstance;

import si.fri.mag.gasperin.cep.h2.CepRule;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class CEPListener implements UpdateListener {
	
	CepRule cepRule;
	
	public CEPListener(CepRule cr){
		this.cepRule = cr;
	}
	
 public void update(EventBean[] newData, EventBean[] oldData) {
	 System.out.println("----------------------------------------------------------------------");
	 System.out.println("CEP LISTENER UPDATE : /" + Constants.CSE_ID + "/" + Constants.CSE_NAME + "/" + this.cepRule.deviceName + "/" + this.cepRule.dataName + " -- VALUE: " + newData[0].getUnderlying());
	 //System.out.println(newData[0].getUnderlying()); // DATA WHICH CAUSED CEP REISE
	 System.out.println("----------------------------------------------------------------------");
	 
	 String content_cep = getSensorDataRep(newData[0].getUnderlying().toString());
	 String targetId_cep = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME + "/" + this.cepRule.deviceName + "/" + this.cepRule.dataName;
	 ContentInstance cin_cep = new ContentInstance();
	 cin_cep.setContent(content_cep);
	 cin_cep.setContentInfo(MimeMediaType.OBIX);
	 RequestSender.createContentInstance(targetId_cep, cin_cep);
	 
 }
 
 public boolean equals(CEPListener listener){
	 if(listener.cepRule.equals(this.cepRule)){
		 return true;
	 }else{
		 return false;
	 }
 }
 
 private static String getSensorDataRep(String value) {
	Obj obj = new Obj();
	obj.add(new Str("data", value));
	return ObixEncoder.toString(obj);
}
 

}