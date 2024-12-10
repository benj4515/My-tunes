module dk.easv.mytunes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;
    requires java.naming;
    requires javafx.media;
    requires java.desktop;


    opens dk.easv.mytunes to javafx.fxml;
    opens dk.easv.mytunes.BE to javafx.base;
    exports dk.easv.mytunes;
    exports dk.easv.mytunes.GUI.Controller;
    opens dk.easv.mytunes.GUI.Controller to javafx.fxml;
}