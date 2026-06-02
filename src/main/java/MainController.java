import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainController {

    @FXML private ComboBox<String> testCombo;
    @FXML private TextField sliceField;
    @FXML private Button runButton;
    @FXML private TextArea sourceArea;
    @FXML private ScrollPane graphScrollPane;
    @FXML private ImageView graphView;
    @FXML private TextArea textOutputArea;
    @FXML private Label placeholderLabel;
    @FXML private Label zoomLabel;
    @FXML private StackPane centerPane;
    @FXML private ListView<GraphStage> stagesList;

    private final ObservableList<GraphStage> stages = FXCollections.observableArrayList();
    private static final String TEST_DIR = "src/main/resources/cfg";

    private double zoom = 1.0;
    private static final double ZOOM_STEP = 0.2;
    private static final double ZOOM_MIN = 0.1;
    private static final double ZOOM_MAX = 10.0;

    @FXML
    public void initialize() {
        loadTestFiles();

        stagesList.setCellFactory(lv -> new StageCell());
        stagesList.setItems(stages);

        stagesList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null) showStage(sel);
                });

        testCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null) loadSourcePreview(sel);
                });

        resetStages(false);
        showPlaceholder();

        graphView.setPreserveRatio(true);

        // Asegurar que scroll pane y text area llenen el StackPane
        graphScrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        textOutputArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        graphScrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() > 0) zoomIn();
                else zoomOut();
                e.consume();
            }
        });
    }

    private void loadTestFiles() {
        File dir = new File(TEST_DIR);
        testCombo.getItems().clear();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, n) -> n.endsWith(".txt"));
            if (files != null) {
                Arrays.sort(files);
                for (File f : files) {
                    testCombo.getItems().add(f.getName());
                }
            }
        }
        if (!testCombo.getItems().isEmpty()) {
            testCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadSourcePreview(String filename) {
        try {
            File f = new File(TEST_DIR + "/" + filename);
            sourceArea.setText(Files.readString(f.toPath()));
        } catch (IOException e) {
            sourceArea.setText("Error al cargar el archivo: " + e.getMessage());
        }
    }

    private void resetStages(boolean withSlice) {
        stages.clear();
        stages.add(new GraphStage("1. Parsing (AST)"));
        stages.add(new GraphStage("2. CFG"));
        stages.add(new GraphStage("3. CFG + PDOM"));
        stages.add(new GraphStage("4. PDT"));
        stages.add(new GraphStage("5. CDG"));
        stages.add(new GraphStage("6. Reaching Defs"));
        stages.add(new GraphStage("7. DDG"));
        stages.add(new GraphStage("8. PDG"));
        stages.add(new GraphStage("9. Slice" + (withSlice ? "" : " (sin criterio)")));
        stages.add(new GraphStage("10. Sliced CFG" + (withSlice ? "" : " (sin criterio)")));
    }

    @FXML
    public void runPipeline() {
        String selectedTest = testCombo.getValue();
        if (selectedTest == null) return;

        String sliceText = sliceField.getText().trim();
        Integer sliceCriterion = null;
        if (!sliceText.isEmpty()) {
            try {
                sliceCriterion = Integer.parseInt(sliceText);
            } catch (NumberFormatException e) {
                showError("El Node ID debe ser un número entero.");
                return;
            }
        }

        resetStages(sliceCriterion != null);
        showPlaceholder();
        runButton.setDisable(true);
        testCombo.setDisable(true);

        String inputFile = TEST_DIR + "/" + selectedTest;
        final Integer finalSlice = sliceCriterion;

        Runnable onUpdate = () -> {
            stagesList.refresh();
            // Auto-seleccionar la última etapa completada
            for (int i = stages.size() - 1; i >= 0; i--) {
                if (stages.get(i).getStatus() == GraphStage.Status.DONE) {
                    stagesList.getSelectionModel().select(i);
                    break;
                }
            }
        };

        PipelineRunner runner = new PipelineRunner(
                inputFile, finalSlice, new ArrayList<>(stages), onUpdate);

        runner.setOnSucceeded(e -> {
            runButton.setDisable(false);
            testCombo.setDisable(false);
        });

        runner.setOnFailed(e -> {
            runButton.setDisable(false);
            testCombo.setDisable(false);
            Throwable t = runner.getException();
            String msg = t != null ? t.getMessage() : "Error desconocido";
            showError("Pipeline falló: " + msg);
            // Mark current running stage as error
            for (GraphStage s : stages) {
                if (s.getStatus() == GraphStage.Status.RUNNING) {
                    s.setStatus(GraphStage.Status.ERROR);
                }
            }
            stagesList.refresh();
        });

        Thread thread = new Thread(runner);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void zoomIn() {
        setZoom(zoom + ZOOM_STEP);
    }

    @FXML
    public void zoomOut() {
        setZoom(zoom - ZOOM_STEP);
    }

    @FXML
    public void zoomFit() {
        if (graphView.getImage() == null) return;
        double paneW = graphScrollPane.getWidth() - 20;
        double paneH = graphScrollPane.getHeight() - 20;
        double imgW = graphView.getImage().getWidth();
        double imgH = graphView.getImage().getHeight();
        if (paneW <= 0 || paneH <= 0 || imgW <= 0 || imgH <= 0) return;
        setZoom(Math.min(paneW / imgW, paneH / imgH));
    }

    private void setZoom(double newZoom) {
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, newZoom));
        applyZoom();
        if (zoomLabel != null)
            zoomLabel.setText(String.format("%.0f%%", zoom * 100));
    }

    private void applyZoom() {
        if (graphView.getImage() == null) return;
        graphView.setFitWidth(graphView.getImage().getWidth() * zoom);
        graphView.setFitHeight(graphView.getImage().getHeight() * zoom);
    }

    private void showStage(GraphStage stage) {
        if (stage.getStatus() != GraphStage.Status.DONE) return;

        if (stage.hasGraph()) {
            graphScrollPane.setVisible(true);
            textOutputArea.setVisible(false);
            placeholderLabel.setVisible(false);
            graphView.setImage(stage.getRenderedImage());
            Platform.runLater(this::zoomFit);
        } else if (stage.hasText()) {
            graphScrollPane.setVisible(false);
            textOutputArea.setVisible(true);
            placeholderLabel.setVisible(false);
            textOutputArea.setText(stage.getTextContent());
        } else {
            showPlaceholder();
        }
    }

    private void showPlaceholder() {
        graphScrollPane.setVisible(false);
        textOutputArea.setVisible(false);
        placeholderLabel.setVisible(true);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // ── Celda personalizada para el ListView de etapas ──────────────────────

    private static class StageCell extends ListCell<GraphStage> {
        @Override
        protected void updateItem(GraphStage item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle(null);
                return;
            }

            String icon;
            String color;
            switch (item.getStatus()) {
                case PENDING  -> { icon = "○ "; color = "#888888"; }
                case RUNNING  -> { icon = "⟳ "; color = "#56a6e0"; }
                case DONE     -> { icon = "✓ "; color = "#6a9955"; }
                case SKIPPED  -> { icon = "— "; color = "#666666"; }
                case ERROR    -> { icon = "✗ "; color = "#cc4444"; }
                default       -> { icon = "  "; color = "#888888"; }
            }

            setText(icon + item.getName());
            setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-padding: 5 8 5 8;");
        }
    }
}
