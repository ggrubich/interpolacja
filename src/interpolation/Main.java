package interpolation;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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

        final HBox menu = new HBox();
        menu.setSpacing(6.0);

        addXField = new TextField();
        HBox.setHgrow(addXField, Priority.ALWAYS);
        addXField.setPromptText("x");
        addXField.setOnAction(this::onAdd);

        addYField = new TextField();
        HBox.setHgrow(addYField, Priority.ALWAYS);
        addYField.setPromptText("y");
        addYField.setOnAction(this::onAdd);

        final Button addButton = new Button("Add");
        addButton.setMinWidth(45);
        addButton.setOnAction(this::onAdd);

        menu.getChildren().addAll(addXField, addYField, addButton);

        getChildren().addAll(table, menu);
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
}

class ResultView extends VBox {
    private final Property<Interpolation> interpolation;
    private final Text polynomial;

    public ResultView(Property<Interpolation> interpolation_) {
        super();
        interpolation = interpolation_;

        setPadding(new Insets(10.0, 20.0, 10.0, 20.0));
        setSpacing(10.0);
        setPrefWidth(800);
        setMinWidth(200);
        HBox.setHgrow(this, Priority.ALWAYS);

        final Label polynomialLabel = new Label("Polynomial:");

        polynomial = new Text();
        polynomial.setFont(new Font(30));
        polynomial.wrappingWidthProperty().bind(widthProperty().multiply(0.9));
        updatePolynomial();

        getChildren().addAll(polynomialLabel, polynomial);

        interpolation.addListener(change -> updatePolynomial());
    }

    private void updatePolynomial() {
        polynomial.setText("P(x) = " + interpolation.getValue().getResult().toString());
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
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
