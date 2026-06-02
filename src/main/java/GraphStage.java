import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class GraphStage {

    public enum Status { PENDING, RUNNING, DONE, SKIPPED, ERROR }

    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.PENDING);
    private String dotSource;
    private Image renderedImage;
    private String textContent;

    public GraphStage(String name) {
        this.name.set(name);
    }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public Status getStatus() { return status.get(); }
    public void setStatus(Status s) { status.set(s); }
    public ObjectProperty<Status> statusProperty() { return status; }

    public String getDotSource() { return dotSource; }
    public void setDotSource(String d) { this.dotSource = d; }

    public Image getRenderedImage() { return renderedImage; }
    public void setRenderedImage(Image i) { this.renderedImage = i; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String t) { this.textContent = t; }

    public boolean hasGraph() { return renderedImage != null; }
    public boolean hasText() { return textContent != null; }
}
