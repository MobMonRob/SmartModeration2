package dhbw.smartmoderation.data.model;

public abstract class ModelClass {

	protected boolean IsDeleted;

	public boolean isDeleted(){
		return IsDeleted;
	}

	public void setIsDeleted(boolean isDeleted){
		this.IsDeleted = isDeleted;
	}
}
