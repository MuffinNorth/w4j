module ru.muffinnorth.w4j {
    requires javafx.controls;
    requires javafx.fxml;
    requires jep;

    opens ru.muffinnorth.w4j to javafx.fxml;
    exports ru.muffinnorth.w4j;
    opens ru.muffinnorth.w4j.controller to javafx.fxml;
    exports ru.muffinnorth.w4j.controller;
}