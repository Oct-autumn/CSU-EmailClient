module com.example.csuemailclient {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires lombok;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires mail;

    exports cn.octautumn.csuemailclient.controller;
    opens cn.octautumn.csuemailclient.controller to javafx.fxml;
    opens cn.octautumn.csuemailclient.controller.customcontrols to javafx.fxml;
    opens cn.octautumn.csuemailclient to javafx.graphics;
    opens cn.octautumn.csuemailclient.config to com.fasterxml.jackson.databind;
    opens cn.octautumn.csuemailclient.common to javafx.graphics;
}