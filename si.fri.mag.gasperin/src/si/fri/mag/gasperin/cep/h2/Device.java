package si.fri.mag.gasperin.cep.h2;

public class Device {
	
	public String deviceName;
	public int id;
	
	public Device(int id, String deviceName){
		this.deviceName = deviceName;
		this.id = id;
	}
	
	public String toString(){
		return "Id: " + id + "Device name: " + deviceName ;
	}
	
	 public boolean equals(Device device){
		 if(device.deviceName.equals(this.deviceName) && device.id == this.id){
			 return true;
		 }else{
			 return false;
		 }
	 }
}
