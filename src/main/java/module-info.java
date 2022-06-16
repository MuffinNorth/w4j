module ru.muffinnorth.w4j {
    requires javafx.controls;
    requires javafx.fxml;
    requires jep;
    requires jfreechart.fx;
    requires jfreechart;
    requires com.google.common;
    requires lombok;
    requires org.apache.commons.lang3;

    opens ru.muffinnorth.w4j to javafx.fxml;
    exports ru.muffinnorth.w4j;
    opens ru.muffinnorth.w4j.controller to javafx.fxml;
    exports ru.muffinnorth.w4j.controller;
}