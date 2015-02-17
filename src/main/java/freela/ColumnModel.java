package freela;

import java.io.Serializable;

public class ColumnModel implements Serializable{
	private String header;
	private String name;
	private int id;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ColumnModel(){
		
	}
	public ColumnModel(String h,String n){
		this.header=h;
		this.name=n;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}