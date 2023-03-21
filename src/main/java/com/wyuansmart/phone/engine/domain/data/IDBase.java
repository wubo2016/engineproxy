package com.wyuansmart.phone.engine.domain.data;


public class IDBase {
	
	private String id = "";
	
	private String name = "";
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setId(long id) {
		this.id = Long.toString(id);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name !=  null) {
			this.name = name;
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		} else if (object == this) {
			return true;
		}

		if (object instanceof IDBase) {
			IDBase info = (IDBase) object;

			if (this.getId() == null && info.getId() != null) {
				return false;
			} else if (this.getId() != null && !this.getId().equals(info.getId())) {
				return false;
			} else if (this.getName() == null && info.getName() != null) {
				return false;
			} else if (this.getName() != null && !this.getName().equals(info.getName())) {
				return false;
			} else {
				return true;
			}
		
		}
		return false;
	}
}
