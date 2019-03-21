package UnemployedVoodooFamily.GUI.Content;

import UnemployedVoodooFamily.Data.DailyFormattedDataModel;
import UnemployedVoodooFamily.Data.Enums.Data;
import UnemployedVoodooFamily.Data.RawTimeDataModel;
import UnemployedVoodooFamily.Data.WeeklyFormattedDataModel;
import UnemployedVoodooFamily.Logic.FormattedTimeDataLogic;
import UnemployedVoodooFamily.Logic.Listeners.DataLoadedListener;
import UnemployedVoodooFamily.Logic.RawTimeDataLogic;
import UnemployedVoodooFamily.Logic.Session;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class TableViewController implements DataLoadedListener {

    @FXML
    private Tab rawDataTab;

    @FXML
    private Tab formattedDataTab;

    @FXML
    private TableView rawData;

    @FXML
    private TableView weeklyTable;

    @FXML
    private ToggleButton weeklyToggleBtn;

    @FXML
    private ToggleButton monthlyToggleBtn;

    @FXML
    private Button exportBtn;

    @FXML
    private GridPane tableRoot;

    @FXML
    private Label rawStartDate;
    @FXML
    private Label rawEndDate;
    @FXML
    private Pane summaryRoot;

    @FXML
    private VBox filterBox;
    @FXML
    private MenuButton projectFilterBtn;
    @FXML
    private MenuButton workspaceFilterBtn;

    private Node weeklySummary;
    private Node monthlySummary;
    private TableView monthlyTable;


    private final ToggleGroup timeSpanToggleGroup = new ToggleGroup();

    private RawTimeDataLogic rawTimeDataLogic = new RawTimeDataLogic();
    private FormattedTimeDataLogic formattedTimeDataLogic = new FormattedTimeDataLogic();
    private EnumSet<Data> loadedData = EnumSet.noneOf(Data.class);

    private Set<Object> filterOptions = new HashSet<>();


    public Node loadFXML() throws IOException {
        URL r = getClass().getClassLoader().getResource("Table.fxml");
        return FXMLLoader.load(r);
    }

    // |##################################################|
    // |                GENERAL METHODS                   |
    // |##################################################|

    public void initialize() {
        Session.getInstance().addListener(this);
        setupUIElements();
        setKeyAndClickListeners();
    }

    /**
     * Sets up the UI elements
     */
    private void setupUIElements() {

        try {
            this.weeklySummary = new WeeklySummaryViewController().loadFXML();
            this.monthlySummary = new MonthlySummaryViewController().loadFXML();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        buildFormattedMonthlyTable();
        buildFormattedWeeklyTable();
        buildRawDataTable();
        weeklyToggleBtn.setToggleGroup(timeSpanToggleGroup);
        monthlyToggleBtn.setToggleGroup(timeSpanToggleGroup);
        weeklyToggleBtn.setSelected(true);
    }

    /**
     * Sets input actions on UI elements
     */
    private void setKeyAndClickListeners() {
        exportBtn.setOnAction(event -> {
            formattedTimeDataLogic.exportToExcelDocument();
        });


        formattedDataTab.setOnSelectionChanged(event -> {
            if(formattedDataTab.isSelected()) {
                buildWeeklyTable();
            }
        });


        weeklyToggleBtn.setOnAction((ActionEvent e) -> {
            switchView(tableRoot, weeklyTable);
            switchView(summaryRoot, weeklySummary);
        });
        monthlyToggleBtn.setOnAction((ActionEvent e) -> {
            switchView(tableRoot, monthlyTable);
            switchView(summaryRoot, monthlySummary);
        });
    }

    // |##################################################|
    // |               RAW DATA TABLE METHODS             |
    // |##################################################|

    /**
     * Builds the viewable table of all the raw from the Toggl user's data
     */
    private void buildRawDataTable() {
        //Clears the already existing data in the table
        clearTable(rawData);

        //Create all columns necessary
        TableColumn<RawTimeDataModel, String> projectCol = new TableColumn<>("Project");
        projectCol.setCellValueFactory(new PropertyValueFactory<>("project"));

        TableColumn<RawTimeDataModel, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<RawTimeDataModel, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<RawTimeDataModel, String> startTimeCol = new TableColumn<>("Start Time");
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        TableColumn<RawTimeDataModel, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        TableColumn<RawTimeDataModel, String> endTimeCol = new TableColumn<>("End Time");
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        TableColumn<RawTimeDataModel, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        //Adds the columns to the table and updates it
        rawData.setEditable(true);
        rawData.getColumns()
               .addAll(projectCol, descCol, startDateCol, startTimeCol, endDateCol, endTimeCol, durationCol);
    }

    private void setRawDataTableData() {
        rawData.getItems().setAll(getObservableRawData());
        String endTime = rawTimeDataLogic.getDataEndTime();
        String startTime = rawTimeDataLogic.getDataStartTime();
        Platform.runLater(() -> {
            rawEndDate.setText(endTime);
            rawStartDate.setText(startTime);
        });

    }

    private void setFormattedTableData() {
        weeklyTable.getItems().setAll(getObservableWeeklyData());
        monthlyTable.getItems().setAll(getObservableMonthlyData());
    }

    /**
     * Creates an observable list containing RawTimeDataModel objects
     * @return an ObservableList containing RawTimeDatModel objects
     */
    private ObservableList<RawTimeDataModel> getObservableRawData() {
        return rawTimeDataLogic.buildObservableRawTimeData();
    }

    // |##################################################|
    // |           FORMATTED DATA TABLE METHODS           |
    // |##################################################|

    /**
     * Builds the table for the formatted table tab. Builds either weekly or monthly depending on which is selected by
     * the toggle buttons
     */
    private void buildWeeklyTable() {
        buildFormattedWeeklyTable();
    }

    private void buildMonthlyTable() {
        buildFormattedMonthlyTable();
    }

    /**
     * Builds a formatted table with a weekly overview
     */
    @SuppressWarnings("Duplicates")
    private void buildFormattedWeeklyTable() {
        //Clears the already existing data in the table
        clearTable(weeklyTable);

        //Create all columns necessary
        TableColumn<DailyFormattedDataModel, String> weekDayCol = new TableColumn<>("Week Day");
        weekDayCol.setCellValueFactory(new PropertyValueFactory<>("weekDay"));
        weekDayCol.setSortable(false);

        TableColumn<DailyFormattedDataModel, Double> workedHoursCol = new TableColumn<>("Worked Hours");
        workedHoursCol.setCellValueFactory(new PropertyValueFactory<>("workedHours"));
        workedHoursCol.setSortable(false);
        workedHoursCol.setCellFactory(col -> setDoubleFormatter());

        TableColumn<DailyFormattedDataModel, Double> supposedHoursCol = new TableColumn<>("Supposed Hours");
        supposedHoursCol.setCellValueFactory(new PropertyValueFactory<>("supposedHours"));
        supposedHoursCol.setSortable(false);
        supposedHoursCol.setCellFactory(col -> setDoubleFormatter());

        TableColumn<DailyFormattedDataModel, Double> overtimeCol = new TableColumn<>("Overtime");
        overtimeCol.setCellValueFactory(new PropertyValueFactory<>("overtime"));
        overtimeCol.setSortable(false);
        DecimalFormat df = new DecimalFormat("#0.00 ");
        overtimeCol.setCellFactory(col -> new TableCell<DailyFormattedDataModel, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    setText(df.format(item));
                    setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()));
                    if(item < 0.0) {
                        setTextFill(Color.RED); // or use setStyle(String)
                    }
                    else if(item > 0.0) {
                        setTextFill(Color.GREEN); // or use setStyle(String)
                    }
                    else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        //Adds the columns to the table and updates it
        weeklyTable.getColumns().addAll(weekDayCol, workedHoursCol, supposedHoursCol, overtimeCol);
        weeklyTable.setEditable(false);
    }

    /**
     * Builds a formatted table with monthly overview
     */
    @SuppressWarnings("Duplicates")
    private void buildFormattedMonthlyTable() {
        //Clears the already existing data in the table

        this.monthlyTable = new TableView();
        clearTable(weeklyTable);

        //Create all columns necessary

        TableColumn<WeeklyFormattedDataModel, Integer> weekNumbCol = new TableColumn<>("Week Number");
        weekNumbCol.setCellValueFactory(new PropertyValueFactory<>("weekNumber"));
        weekNumbCol.setSortable(false);

        TableColumn<WeeklyFormattedDataModel, Double> workedHoursCol = new TableColumn<>("Worked Hours");
        workedHoursCol.setCellValueFactory(new PropertyValueFactory<>("workedHours"));
        workedHoursCol.setSortable(false);
        workedHoursCol.setCellFactory(col -> setDoubleFormatter());

        TableColumn<WeeklyFormattedDataModel, Double> supposedHoursCol = new TableColumn<>("Supposed Hours");
        supposedHoursCol.setCellValueFactory(new PropertyValueFactory<>("supposedHours"));
        supposedHoursCol.setSortable(false);
        supposedHoursCol.setCellFactory(col -> setDoubleFormatter());

        TableColumn<WeeklyFormattedDataModel, Double> overtimeCol = new TableColumn<>("Overtime");
        overtimeCol.setCellValueFactory(new PropertyValueFactory<>("overtime"));
        overtimeCol.setSortable(false);

        DecimalFormat df = new DecimalFormat("#0.00 ");
        overtimeCol.setCellFactory(col -> new TableCell<WeeklyFormattedDataModel, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    setText(df.format(item));
                    setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, Font.getDefault().getSize()));
                    if(item < 0.0) {
                        setTextFill(Color.RED); // or use setStyle(String)
                    }
                    else if(item > 0.0) {
                        setTextFill(Color.GREEN); // or use setStyle(String)
                    }
                    else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        //Adds the columns to the table and updates it
        monthlyTable.getColumns().addAll(weekNumbCol, workedHoursCol, supposedHoursCol, overtimeCol);
        monthlyTable.setEditable(false);
    }

    private <T> TableCell<T, Double> setDoubleFormatter() {
        DecimalFormat df = new DecimalFormat("#0.00 ");
        return new TableCell<T, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    setText(df.format(item));
                }
            }
        };
    }

    /**
     * Creates an observable list containing WeeklyTimeDataModel objects
     * @return an ObservableList containing WeeklyTimeDatModel objects
     */
    private ObservableList<DailyFormattedDataModel> getObservableWeeklyData() {
        return formattedTimeDataLogic.buildObservableWeeklyTimeData(Session.getInstance().getTimeEntries());
    }

    /**
     * Creates an observable list containing MonthlyTimeDataModel objects
     * @return an ObservableList containing MonthlyTimeDatModel objects
     */
    private ObservableList<WeeklyFormattedDataModel> getObservableMonthlyData() {
        return formattedTimeDataLogic.buildObservableMonthlyTimeData();
    }

    // |##################################################|
    // |                  OTHER METHODS                   |
    // |##################################################|

    /**
     * Clears the selected TableView
     * @param table the TableView to clear
     */
    private void clearTable(TableView table) {
        table.getColumns().clear();
        table.getItems().clear();
    }

    private <T extends Pane> void switchView(T root, Node content) {
        ObservableList<Node> children = root.getChildren();
        if(children.isEmpty()) {
            children.addAll(content);
        }
        else if(! children.contains(content)) {
            children.clear();
            children.addAll(content);
        }
    }

    /**
     * fill all filter buttons with the necessary buttons and data
     */
    private void setFilterOptions() {
        Session session = Session.getInstance();

        buildFilterButton(projectFilterBtn);
        buildFilterButton(workspaceFilterBtn);

        session.getProjects()
               .forEach((project -> projectFilterBtn.getItems().add(new CheckMenuObject(project, project.getName()))));
        session.getWorkspaces().forEach(
                (project -> workspaceFilterBtn.getItems().add(new CheckMenuObject(project, project.getName()))));
    }


    /**
     * Initialize a button with all items common for filter buttons,
     * including "select all" and "deselect buttons"
     * @param button the MenuButton to initialize
     */
    private void buildFilterButton(MenuButton button) {
        MenuItem selectAll = new MenuItem("Select all");
        MenuItem deselectAll = new MenuItem("Deselect all");
        selectAll.setStyle("-fx-font-weight: bold;");
        deselectAll.setStyle("-fx-font-weight: bold;");
        button.getItems().add(selectAll);
        button.getItems().add(deselectAll);
        button.getItems().add(new SeparatorMenuItem());
    }

    //add generic object to filter options set
    private <T> void addFilterOption(T t) {
        filterOptions.add(t);
    }

    //remove generic object from filter options set
    private <T> void removeFilterOption(T t) {
        filterOptions.remove(t);
    }

    /**
     * Inner class to create a menu-item with a check box and an object attached to it.
     */
    class CheckMenuObject extends CustomMenuItem {
        private Object object;

        public CheckMenuObject(Object object, String name) {
            super();
            this.object = object;
            setHideOnClick(false);
            CheckBox cb = new CheckBox(name);
            setGraphic(cb);
            setContent(cb);
            cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    addFilterOption(object);
                }
                else {
                    removeFilterOption(object);
                }
            });
            cb.setSelected(true);

        }
    }


    /**
     * Set newly loaded data to the tables, only when
     * all the necessary data has been loaded
     * @param e
     */
    @Override
    public void dataLoaded(Data e) {
        loadedData.add(e);
        //check if necassary data is loaded
        if(loadedData.containsAll(EnumSet.of(Data.TIME_ENTRIES))) {
            setFormattedTableData();
            if(loadedData.containsAll(EnumSet.of(Data.TIME_ENTRIES, Data.PROJECTS, Data.TASKS, Data.WORKSPACES))) {
                setRawDataTableData();
                setFilterOptions();
                loadedData = EnumSet.noneOf(Data.class); //empty the set, readying it for next
            }
        }

    }
}
