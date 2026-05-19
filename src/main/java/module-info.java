module com.spiky.jsonflattener {
    requires javafx.controls;
    requires javafx.fxml;
    requires static org.jetbrains.annotations;
    requires tools.jackson.databind;


    opens com.spiky.jsonflattener to javafx.fxml;
    exports com.spiky.jsonflattener;
}