package view_controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Appointment;
import model.Schedule;
import utilities.NewWindow;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CalendarMainWindowController implements Initializable {
    @FXML
    private Button viewByMonthButton;
    @FXML
    private Button viewByweekButton;
    @FXML
    private Button nextTimeframe;
    @FXML
    private Button previousTimeFrame;
    @FXML
    private Pane byMonthPane;
    @FXML
    private Pane byWeekPane;
    @FXML
    private GridPane byMonthGridPane;
    @FXML
    private GridPane byWeekGridPane;
    @FXML
    private Button returnToMainWindowButton;
    @FXML
    private Label calendarMainWindowLabel;
    @FXML
    private Label sundayLabel;
    @FXML
    private Label mondayLabel;
    @FXML
    private Label tuesdayLabel;
    @FXML
    private Label wednesdayLabel;
    @FXML
    private Label thursdayLabel;
    @FXML
    private Label fridayLabel;
    @FXML
    private Label saturdayLabel;
    @FXML
    private Text currentTimeFrameLabel;
    @FXML
    private Text currentWeekLabel;

    LocalDate currentDate = LocalDate.now();
    int thisYearInt = currentDate.getYear();
    Month thisMonth = currentDate.getMonth();
    Month nextMonth;
    boolean byWeekTimeFrame;

    public void loadMainWindow(ActionEvent event) throws IOException {
        NewWindow.display((Stage) calendarMainWindowLabel.getScene().getWindow(),
                getClass().getResource("MainWindow.fxml"));
    }
    public String calculateWeekRange(LocalDate currentDate){
        int daysSinceSunday = currentDate.getDayOfWeek().getValue();
        LocalDate weekStartDay = currentDate.minus(Period.ofDays(daysSinceSunday));
        LocalDate weekEndDay = weekStartDay.plus(Period.ofDays(6));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy");
        String formattedStringStart = weekStartDay.format(formatter);
        String formattedStringEnd = weekEndDay.format(formatter);
        return formattedStringStart + " - " + formattedStringEnd;
    }

    public void showCalendarByWeek(ActionEvent event) {
        byWeekTimeFrame = true;
        byMonthPane.setVisible(false);
        byWeekPane.setVisible(true);
        currentTimeFrameLabel.setText(String.valueOf(thisMonth));
        String weekRange = calculateWeekRange(currentDate);
        currentTimeFrameLabel.setText(weekRange);
    }
    public void showCalendarByMonth(ActionEvent event) {
        byWeekTimeFrame = false;
        byWeekPane.setVisible(false);
        byMonthPane.setVisible(true);
        currentTimeFrameLabel.setText(String.valueOf(thisMonth));
    }

    public void nextTimeFrame(ActionEvent event) {
        if (byWeekTimeFrame){
            resetGridLines(byWeekGridPane);
            String weekRangeString = calculateWeekRange(currentDate.plus(Period.ofDays(6)));
            currentDate = currentDate.plus(Period.ofDays(6));
            currentTimeFrameLabel.setText(weekRangeString);
            populateCalendar(currentTimeFrameLabel, "incr");
        } else {
            resetGridLines(byMonthGridPane);
            populateCalendar(currentTimeFrameLabel, "incr");
        }
        return;
    }

    public void previousTimeFrame(ActionEvent event) {
        if (byWeekTimeFrame){
            resetGridLines(byWeekGridPane);
            String weekRange = calculateWeekRange(currentDate.minus(Period.ofDays(6)));
            currentDate = currentDate.minus(Period.ofDays(6));
            currentTimeFrameLabel.setText(weekRange);
            populateCalendar(currentTimeFrameLabel, "decr");
        } else {
            resetGridLines(byMonthGridPane);
            populateCalendar(currentTimeFrameLabel, "decr");
        }
        return;

    }

    public void resetGridLines(GridPane grid){
        Node gridLines = grid.getChildren().get(0);
        grid.getChildren().clear();
        grid.getChildren().add(0, gridLines);
    }
    //TODO refactor this method, extract code into multiple small methods
    public void populateCalendar(Text currentTimeFrameLabel, String direction) {
        if (byWeekTimeFrame){
            String timeFrame = currentTimeFrameLabel.getText();
            String dateSplit[] = timeFrame.split(" - ");
            String startDate = dateSplit[0];
            String endDate = dateSplit[1];
            DateTimeFormatter byWeekTimeFrameFormatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy");
            LocalDate parsedStartDate = LocalDate.parse(startDate, byWeekTimeFrameFormatter);
            LocalDate parsedEndDate = LocalDate.parse(endDate, byWeekTimeFrameFormatter);
            if (thisMonth.equals(Month.DECEMBER) && nextMonth.equals(Month.JANUARY)) thisYearInt += 1;
            if (nextMonth.equals(Month.DECEMBER) && thisMonth.equals(Month.JANUARY)) thisYearInt -= 1;
            ObservableList<Appointment> appointmentsForGivenWeek = Schedule.combineAppointmentsByWeek(parsedStartDate, parsedEndDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
//TODO optimize this code, make it faster
//            int col = firstDayOfTheWeek;
            if (appointmentsForGivenWeek != null) {
                for (int i = 1; i <= 7; i++){
                    for (int j = 0; j < appointmentsForGivenWeek.size(); j++) {
                        String temp[] = appointmentsForGivenWeek.get(j).getAppointmentStart().split(" ");
                        LocalDate localDate = LocalDate.parse(temp[0], formatter);
                        if (parsedStartDate.plusDays(i).equals(localDate)) {
                            Text appointmentText = new Text();
                            appointmentText.setText("Appointment with: " + appointmentsForGivenWeek.get(j).getAppointmentCustomerName());
                            GridPane.setConstraints(appointmentText, i, 0);
                            byWeekGridPane.getChildren().addAll(appointmentText);
                        }
                    }
                }
            }

        } else {
            int row = 0;
            int col;
            thisMonth = Month.valueOf(currentTimeFrameLabel.getText());
            nextMonth = direction.equals("incr") ? thisMonth.plus(1) : thisMonth.minus(1);
            if (thisMonth.equals(Month.DECEMBER) && nextMonth.equals(Month.JANUARY)) thisYearInt += 1;
            if (nextMonth.equals(Month.DECEMBER) && thisMonth.equals(Month.JANUARY)) thisYearInt -= 1;
            currentTimeFrameLabel.setText(nextMonth.toString());
            LocalDate dte = LocalDate.of(currentDate.getYear(), nextMonth, 1);
            int firstDayOfTheMonth = dte.getDayOfWeek().getValue();
            int totalDaysInMonth = dte.lengthOfMonth();
            ObservableList<Appointment> appointmentsForGivenMonth = Schedule.combineAppointmentsByMonth(nextMonth, thisYearInt);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
//TODO optimize this code, make it faster
            col = firstDayOfTheMonth;
            if (appointmentsForGivenMonth != null) {
                for (int i = 1; i <= totalDaysInMonth; i++){
                    if (col == 7) {
                        row += 1;
                        col = 0;
                    }
                    for (int j = 0; j < appointmentsForGivenMonth.size(); j++) {
                        String temp[] = appointmentsForGivenMonth.get(j).getAppointmentStart().split(" ");
                        LocalDate localDate = LocalDate.parse(temp[0], formatter);
                        int apptDay = localDate.getDayOfMonth();
                        if (apptDay == i) {
                            Text test = new Text();
                            test.setText("Appointment with: " + appointmentsForGivenMonth.get(j).getAppointmentCustomerName());
                            GridPane.setConstraints(test, col, row);
                            byMonthGridPane.getChildren().addAll(test);
                        }
                    }
                    col += 1;
                }
            }

        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        byWeekPane.setVisible(false);
        byMonthPane.setVisible(true);
        byWeekTimeFrame = false;
        currentTimeFrameLabel.setText(thisMonth.minus(1).toString());
        populateCalendar(currentTimeFrameLabel, "incr");
    }
}
