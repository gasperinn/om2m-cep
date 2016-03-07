package si.fri.mag.gasperin.cep.h2;

public class CepRule {
	
	public String deviceName;
	public String dataName;
	public String rule;
	public int id;
	
	public CepRule(int id, String deviceName, String dataName, String rule){
		this.deviceName = deviceName;
		this.dataName = dataName;
		this.rule = rule;
		this.id = id;
	}
	
	public String toString(){
		return "Id: " + id + "Device name: " + deviceName + " Data name: " + dataName + " Rule: " + rule ;
	}
	
	 public boolean equals(CepRule rule){
		 if(rule.deviceName.equals(this.deviceName) && rule.dataName.equals(this.dataName) && rule.rule.equals(this.rule) && rule.id == this.id ){
			 return true;
		 }else{
			 return false;
		 }
	 }
}
