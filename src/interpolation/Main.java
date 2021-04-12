package interpolation;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

// Custom TableCell subclass that handles parse errors correctly.
class RationalCell extends TableCell<Point, Rational> {
    private final TextField textField = new TextField();

    public RationalCell() {
        super();
        itemProperty().addListener((obs, oldItem, newItem) -> {
            setText(newItem == null ? null : newItem.toString());
        });
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        setGraphic(textField);
        textField.setOnAction(event -> {
            update();
            event.consume();
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.setText(getText());
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(Rational val) {
        super.commitEdit(val);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    private void update() {
        try {
            Rational val = Rational.parse(textField.getText());
            commitEdit(val);
        }
        catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.show();
        }
    }
}

class InputView extends VBox {
    private final ObservableList<Point> points;
    private final TableView<Point> table;
    private final TextField addXField;
    private final TextField addYField;

    public InputView(ObservableList<Point> points_) {
        super();
        points = points_;

        setSpacing(10.0);
        setPadding(new Insets(10.0));
        setPrefWidth(400);
        setMinWidth(200);

        table = new TableView<>();
        table.setItems(points);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        table.setOnKeyPressed(ev -> {
            if (ev.getCode().equals(KeyCode.DELETE)) {
                onDelete(ev);
            }
        });

        final TableColumn<Point, Rational> xColumn = new TableColumn<>("x");
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        xColumn.setCellFactory(col -> new RationalCell());
        xColumn.setOnEditCommit(this::onChangeX);
        // We subtract 1.5 to avoid triggering the scrollbar.
        xColumn.prefWidthProperty().bind(table.widthProperty().divide(2).subtract(1.5));
        table.getSortOrder().add(xColumn);
        table.getColumns().add(xColumn);

        final TableColumn<Point, Rational> yColumn = new TableColumn<>("y");
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        yColumn.setCellFactory(col -> new RationalCell());
        yColumn.setOnEditCommit(this::onChangeY);
        yColumn.prefWidthProperty().bind(table.widthProperty().divide(2).subtract(1.5));
        table.getColumns().add(yColumn);

        final HBox textFields = new HBox();
        textFields.setSpacing(6.0);

        addXField = new TextField();
        HBox.setHgrow(addXField, Priority.ALWAYS);
        addXField.setPromptText("x");
        addXField.setOnAction(this::onAdd);

        addYField = new TextField();
        HBox.setHgrow(addYField, Priority.ALWAYS);
        addYField.setPromptText("y");
        addYField.setOnAction(this::onAdd);

        textFields.getChildren().addAll(addXField, addYField);

        final HBox buttons = new HBox();
        buttons.setSpacing(6.0);

        final Button addButton = new Button("Add");
        addButton.setMinWidth(45);
        addButton.setPrefWidth(60);
        addButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addButton, Priority.ALWAYS);
        addButton.setOnAction(this::onAdd);

        final Button deleteButton = new Button("Delete");
        deleteButton.setMinWidth(60);
        deleteButton.setPrefWidth(60);
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteButton, Priority.ALWAYS);
        deleteButton.setOnAction(this::onDelete);

        final Button clearButton = new Button("Clear");
        clearButton.setMinWidth(55);
        clearButton.setPrefWidth(60);
        clearButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(clearButton, Priority.ALWAYS);
        clearButton.setOnAction(this::onClear);

        buttons.getChildren().addAll(addButton, deleteButton, clearButton);

        getChildren().addAll(table, textFields, buttons);
    }

    private void onChangeX(TableColumn.CellEditEvent<Point, Rational> ev) {
        ev.consume();
        Point point = new Point(ev.getNewValue(), ev.getRowValue().getY());
        int i = ev.getTablePosition().getRow();
        for (int j = 0; j < points.size(); ++j) {
            if (j != i && points.get(j).getX().equals(point.getX())) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Duplicate node " + point.getX());
                alert.show();
                return;
            }
        }
        points.set(i, point);
    }

    private void onChangeY(TableColumn.CellEditEvent<Point, Rational> ev) {
        ev.consume();
        Point point = new Point(ev.getRowValue().getX(), ev.getNewValue());
        int i = ev.getTablePosition().getRow();
        points.set(i, point);
    }

    private void onAdd(ActionEvent ev) {
        ev.consume();
        Rational x, y;
        try {
            x = Rational.parse(addXField.getText());
            y = Rational.parse(addYField.getText());
        }
        catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.show();
            return;
        }
        for (Point p : points) {
            if (p.getX().equals(x)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Duplicate node " + x);
                alert.show();
                addXField.requestFocus();
                return;
            }
        }
        Point point = new Point(x, y);
        if (table.getComparator() == null) {
            points.add(point);
        }
        else {
            int idx = Collections.binarySearch(points, point, table.getComparator());
            points.add(idx >= 0 ? idx : -(idx + 1), point);
        }
        addXField.clear();
        addYField.clear();
        addXField.requestFocus();
    }

    private void onDelete(Event ev) {
        ev.consume();
        TableView.TableViewSelectionModel<Point> sel = table.getSelectionModel();
        points.removeAll(sel.getSelectedItems());
        // Removing rows from the table causes it forget its focused column,
        // which means that editing cells with a keyboard shortcut will not work.
        // To compensate for that, we explicitly focus the first column.
        TableView.TableViewFocusModel<Point> focus = table.getFocusModel();
        focus.focus(focus.getFocusedIndex(), table.getColumns().get(0));
        // Force selection to be the same as focus (this invariant can be violated
        // after deleting multiple nonconsecutive rows).
        sel.clearAndSelect(focus.getFocusedIndex());
    }

    private void onClear(ActionEvent ev) {
        ev.consume();
        points.clear();
    }
}

class CopyButton extends Button {
    private final Supplier<String> supplier;

    public CopyButton(Supplier<String> supplier_) {
        super("Copy");
        supplier = supplier_;
        setFont(new Font(11));
        setMinWidth(45);
        setOnAction(this::onAction);
    }

    protected void onAction(ActionEvent ev) {
        ev.consume();
        ClipboardContent content = new ClipboardContent();
        content.putString(supplier.get());
        Clipboard.getSystemClipboard().setContent(content);
    }
}

class InterpolationChart extends LineChart<Number, Number> {
    private static final Rational marginRatio = new Rational(1, 6);
    private static final int nPlotPoints = 500;
    private static final int nTicks = 20;
    private static final Rational defaultWidth = new Rational(20);

    private final Property<Interpolation> interpolation;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final XYChart.Series<Number, Number> plot;
    private final XYChart.Series<Number, Number> data;

    public InterpolationChart(Property<Interpolation> interpolation_) {
        super(new NumberAxis(), new NumberAxis());
        interpolation = interpolation_;
        xAxis = (NumberAxis) getXAxis();
        yAxis = (NumberAxis) getYAxis();
        xAxis.setLabel("x");
        xAxis.setAutoRanging(false);
        yAxis.setLabel("y");
        yAxis.setForceZeroInRange(false);
        plot = new XYChart.Series<>();
        plot.setName("P(x)");
        data = new XYChart.Series<>();
        data.setName("data points");
        getData().add(plot);
        getData().add(data);
        setAnimated(false);

        draw();
        interpolation.addListener(change -> draw());
    }

    private void draw() {
        plot.getData().clear();
        if (interpolation.getValue().getPoints().size() >= 2) {
            drawMultiple();
        }
        else {
            drawConst();
        }
        data.getData().clear();
        for (Point p : interpolation.getValue().getPoints()) {
            data.getData().add(new XYChart.Data<>(p.getX().toDouble(), p.getY().toDouble()));
        }
    }

    private void drawMultiple() {
        List<Point> points = interpolation.getValue().getPoints();
        Rational min = points.get(0).getX();
        Rational max = points.get(0).getX();
        for (int i = 1; i < points.size(); ++i) {
            Rational x = points.get(i).getX();
            if (x.compareTo(min) < 0) {
                min = x;
            }
            if (x.compareTo(max) > 0) {
                max = x;
            }
        }
        Rational margin = (max.sub(min)).mul(marginRatio);
        Rational start = min.sub(margin);
        Rational stop = max.add(margin);
        drawFunctionInRange(start, stop);
    }

    private void drawConst() {
        Rational middle = new Rational(0);
        if (interpolation.getValue().getPoints().size() > 0) {
            middle = interpolation.getValue().getPoints().get(0).getX();
        }
        Rational half = defaultWidth.div(new Rational(2));
        drawFunctionInRange(middle.sub(half), middle.add(half));
    }

    private void drawFunctionInRange(Rational start, Rational stop) {
        double tickApprox = (stop.toDouble() - start.toDouble()) / (double) nTicks;
        // We construct the tick value from specific multiples to make it more readable.
        double tick = 1.0;
        if (tickApprox >= 1.0) {
            while (tick * 10.0 <= tickApprox) {
                tick *= 10.0;
            }
            while (tick * 5.0 <= tickApprox) {
                tick *= 5.0;
            }
        }
        else {
            while (tick / 10.0 >= tickApprox) {
                tick /= 10.0;
            }
            while (tick / 2.0 >= tickApprox) {
                tick /= 2.0;
            }
        }
        // Since ticks are counted from the lower bound, we need to adjust it properly.
        xAxis.setLowerBound(start.toDouble() - (start.toDouble() % tick));
        xAxis.setUpperBound(stop.toDouble());
        xAxis.setTickUnit(tick);
        Rational step = (stop.sub(start)).div(new Rational(nPlotPoints));
        // Avoid the situation where subtracting modulo from the lower bound
        // leaves an empty space at the start of the chart.
        while (start.toDouble() > xAxis.getLowerBound()) {
            start = start.sub(step);
        }
        for (Rational x = start; x.compareTo(stop) <= 0; x = x.add(step)) {
            Rational y = interpolation.getValue().getResult().eval(x);
            plot.getData().add(new XYChart.Data<>(x.toDouble(), y.toDouble()));
        }
    }
}

class ResultView extends VBox {
    private final Property<Interpolation> interpolation;
    private final Text polyText;
    private Rational evalPoint = new Rational(0);
    private final TextField evalInput;
    private final Text evalText;
    private final InterpolationChart chart;

    public ResultView(Property<Interpolation> interpolation_) {
        super();
        interpolation = interpolation_;

        setPadding(new Insets(10.0, 20.0, 10.0, 20.0));
        setSpacing(10.0);
        setPrefWidth(800);
        setMinWidth(200);
        HBox.setHgrow(this, Priority.ALWAYS);

        // Polynomial
        final Label polyLabel = new Label("Polynomial:");

        final HBox polyBox = new HBox();
        polyBox.setAlignment(Pos.CENTER_LEFT);

        polyText = new Text();
        polyText.setFont(new Font(30));
        polyText.wrappingWidthProperty().bind(widthProperty().subtract(80));

        CopyButton polyCopy = new CopyButton(this::getPolyContent);

        polyBox.getChildren().addAll(polyText, polyCopy);

        // Evaluation
        final Label evalLabel = new Label("Value in point:");

        final HBox evalResultBox = new HBox();
        evalResultBox.setAlignment(Pos.CENTER_LEFT);

        evalText = new Text();
        evalText.setFont(new Font(18));
        evalText.wrappingWidthProperty().bind(widthProperty().subtract(80));

        final CopyButton evalCopy = new CopyButton(() -> getEvalResult().toString());

        evalResultBox.getChildren().addAll(evalText, evalCopy);

        final HBox evalInputBox = new HBox();
        evalInputBox.setSpacing(6.0);

        evalInput = new TextField();
        evalInput.setMinWidth(50);
        evalInput.setFont(new Font(12));
        evalInput.setText(evalPoint.toString());
        evalInput.setOnAction(this::onEvalCommit);

        final Button evalCommit = new Button("Eval");
        evalCommit.setMinWidth(45);
        evalCommit.setFont(new Font(12));
        evalCommit.setOnAction(this::onEvalCommit);

        evalInputBox.getChildren().addAll(evalInput, evalCommit);

        // Chart
        final Label chartLabel = new Label("Chart:");
        chart = new InterpolationChart(interpolation);
        VBox.setVgrow(chart, Priority.ALWAYS);

        getChildren().addAll(
                polyLabel,
                polyBox,
                new Separator(Orientation.HORIZONTAL),
                evalLabel,
                evalResultBox,
                evalInputBox,
                new Separator(Orientation.HORIZONTAL),
                chartLabel,
                chart
        );

        updateAll();
        interpolation.addListener(change -> this.updateAll());
    }

    private void updateAll() {
        updatePolyText();
        updateEvalText();
    }

    private String getPolyContent() {
        return interpolation.getValue().getResult().toString();
    }

    private void updatePolyText() {
        polyText.setText("P(x) = " + getPolyContent());
    }

    private Rational getEvalResult() {
        return interpolation.getValue().getResult().eval(evalPoint);
    }

    private void updateEvalText() {
        evalText.setText("P(" + evalPoint + ") = " + getEvalResult());
    }

    private void onEvalCommit(ActionEvent ev) {
        ev.consume();
        try {
            evalPoint = Rational.parse(evalInput.getText());
            updateEvalText();
        }
        catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.show();
            evalInput.requestFocus();
            return;
        }
    }
}

public class Main extends Application {
    private final ObservableList<Point> points = FXCollections.observableArrayList();
    private final Property<Interpolation> interpolation =
            new SimpleObjectProperty<>(new Interpolation(points));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Interpolation");

        HBox root = new HBox();

        InputView input = new InputView(points);
        root.getChildren().add(input);

        ResultView result = new ResultView(interpolation);
        root.getChildren().add(result);

        points.addListener((ListChangeListener<Point>) change -> {
            boolean reload = false;
            while (change.next()) {
                reload = reload || !change.wasPermutated();
            }
            if (reload) {
                interpolation.setValue(new Interpolation(points));
            }
        });

        primaryStage.minWidthProperty().bind(input.minWidthProperty().add(result.minWidthProperty()));
        primaryStage.setMinHeight(200);
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
