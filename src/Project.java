import java.util.ArrayList;

public class Project {
	public String getName() { return name; }
	public String getDate() { return date; }
	public ArrayList<RequestItem> getItems() { return items; }
	
    String name;
    String date;

    ArrayList<RequestItem> items =
            new ArrayList<>();

    public Project(
            String name,
            String date){

        this.name = name;
        this.date = date;
        }
    @Override
    public String toString() {
        return name;
    }
}
