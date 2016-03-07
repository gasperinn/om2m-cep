package si.fri.mag.gasperin.cep.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class H2DBTableDevices {
	
	private Statement stat;
	private String table = "cep_devices";
	private Connection conn;

	public H2DBTableDevices(){
		
		try{
			//DeleteDbFiles.execute("h2", "cep_rules", true);
			
	        Class.forName("org.h2.Driver");
	        conn = DriverManager.getConnection("jdbc:h2:h2/"+table);
	        stat = conn.createStatement();
	        
	        stat.execute("create table if not exists "+table+"(id bigint primary key auto_increment, device_name varchar(255))");
	        
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean insert(String deviceName){
		try {
			stat.execute("insert into "+table+" values(null, '"+deviceName+"');");

			return true;
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public ArrayList<Device> getAll(){
		try {
			
			ResultSet rs;
	        rs = stat.executeQuery("select * from "+table+";");
	        
	        ArrayList<Device> array = new ArrayList();
	        while (rs.next()) {
	        	array.add(new Device(rs.getInt("id"), rs.getString("device_name")));
	        }
	        
	        if(array.size() != 0){
	        	return array;
	        }else{
	        	return null;
	        }
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public Device get(int id){
		try {
			
			ResultSet rs;
	        rs = stat.executeQuery("select * from "+table+" where id = '" + id + "';");
	        
	        if(rs.next()){
	        	return new Device(rs.getInt("id"), rs.getString("device_name"));
	        }else{
	        	return null;
	        }
	        
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public boolean updateDevice(Device device){
		try {
			
			stat.executeUpdate("update "+table+" set device_name='"+device.deviceName+"';");
	        
	        return true;
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean remove(int id){
		try {
			
			stat.execute("delete from "+table+" where id = '" + id + "';");
	        
	        return true;
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean removeAll(){
		try {
			
			stat.execute("truncate table "+table+";");
	        
	        return true;
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void close(){
		
        try {
			conn.close();
			stat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

