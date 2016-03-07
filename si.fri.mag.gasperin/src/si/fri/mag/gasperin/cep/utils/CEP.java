package si.fri.mag.gasperin.cep.utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import si.fri.mag.gasperin.cep.h2.CepRule;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;


public class CEP {
	
	Configuration cepConfig;
		
	public CEP(Class eventClass){
		ConsoleAppender appender = new ConsoleAppender(new SimpleLayout());
	    Logger.getRootLogger().addAppender(appender);
	    Logger.getRootLogger().setLevel((Level) Level.WARN);
	
	    cepConfig = new Configuration();
	    
	    cepConfig.addEventType(eventClass);
	}
	
	public void addRule(CepRule cepRule){ 
		
		EPServiceProvider provider;
		
		if(EPServiceProviderManager.getExistingProvider(cepRule.deviceName) == null){
			// We setup the engine
		    provider = EPServiceProviderManager.getProvider(cepRule.deviceName, cepConfig);
		}else{
			provider = EPServiceProviderManager.getExistingProvider(cepRule.deviceName);
		}
	   
		
	    // We register an EPL statement
	    EPAdministrator cepAdm = provider.getEPAdministrator();
	    
	    EPStatement cepStatement = cepAdm.createEPL(cepRule.rule, cepRule.id+"");
	    
	    CEPListener listener = new CEPListener(cepRule);
	    
	    cepStatement.addListener(listener);
	}
	
	public void sendEvent(DataInterface data, String deviceName){
			EPServiceProvider provider = EPServiceProviderManager.getExistingProvider(deviceName);
			if(provider != null){
				EPRuntime cepRT = provider.getEPRuntime();
				cepRT.sendEvent(data);
			}
	}
	
	
	public void removeRule(CepRule cepRule){
		
		EPServiceProvider provider = EPServiceProviderManager.getExistingProvider(cepRule.deviceName);
	    EPAdministrator cepAdm = provider.getEPAdministrator();
	    EPStatement cepStatement = cepAdm.getStatement(cepRule.id+"");
	    cepStatement.destroy();
	}
	
	public void editRule(CepRule cepRule){
		
		EPServiceProvider provider = EPServiceProviderManager.getExistingProvider(cepRule.deviceName);
	    EPAdministrator cepAdm = provider.getEPAdministrator();
	  
	    EPStatement cepStatement = cepAdm.getStatement(cepRule.id+"");
	    cepStatement.destroy();
	    
	    CEPListener listener = new CEPListener(cepRule);
	    cepStatement = cepAdm.createEPL(cepRule.rule, cepRule.id+"");
	    cepStatement.addListener(listener);
	    
	}
		
	
	
}
