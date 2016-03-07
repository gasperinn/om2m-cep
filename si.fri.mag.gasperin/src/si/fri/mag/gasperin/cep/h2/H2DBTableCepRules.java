package si.fri.mag.gasperin.cep.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class H2DBTableCepRules {
	
	private Statement stat;
	private String table = "cep_rules";
	private Connection conn;

	public H2DBTableCepRules(){
		
		try{
			//DeleteDbFiles.execute("h2", "cep_rules", true);
			
	        Class.forName("org.h2.Driver");
	        conn = DriverManager.getConnection("jdbc:h2:h2/"+table);
	        stat = conn.createStatement();
	        
	        //stat.execute("DROP TABLE IF EXISTS cep_rules;");
	        stat.execute("create table if not exists "+table+"(id bigint primary key auto_increment, device_name varchar(255), data_name varchar(255), rule varchar(255))");
	        //insert("MY_SENSOR", "CEP_DATA", "select * from Data.win:length(1) having avg(value) > 80");
	        
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean insert(String deviceName, String dataName, String rule){
		try {
			stat.execute("insert into "+table+" values(null, '"+deviceName+"', '"+dataName+"', '"+rule+"');");

			return true;
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public ArrayList<CepRule> getAll(){
		try {
			
			ResultSet rs;
	        rs = stat.executeQuery("select * from "+table+";");
	        
	        ArrayList<CepRule> array = new ArrayList();
	        while (rs.next()) {
	        	array.add(new CepRule(rs.getInt("id"), rs.getString("device_name"), rs.getString("data_name"), rs.getString("rule")));
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
	
	public CepRule get(int id){
		try {
			
			ResultSet rs;
	        rs = stat.executeQuery("select * from "+table+" where id = '" + id + "';");
	        
	        if(rs.next()){
	        	return new CepRule(rs.getInt("id"), rs.getString("device_name"), rs.getString("data_name"), rs.getString("rule"));
	        }else{
	        	return null;
	        }
	        
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public CepRule get(String deviceName, String dataName){
		try {
			
			ResultSet rs;
	        rs = stat.executeQuery("select * from "+table+" where device_name = '" + deviceName + "' AND data_name = '"+ dataName +"';");
	        
	        if(rs.next()){
	        	return new CepRule(rs.getInt("id"), rs.getString("device_name"), rs.getString("data_name"), rs.getString("rule"));
	        }else{
	        	return null;
	        }
	        
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean updateRule(CepRule cepRule){
		try {
			
			stat.executeUpdate("update "+table+" set rule='"+cepRule.rule+"', device_name='"+cepRule.deviceName+"', data_name='"+cepRule.dataName+"' where id='"+cepRule.id+"';");
	        
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
