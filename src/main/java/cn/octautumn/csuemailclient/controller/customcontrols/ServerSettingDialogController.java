package cn.octautumn.csuemailclient.controller.customcontrols;

import cn.octautumn.csuemailclient.CoreResources;
import cn.octautumn.csuemailclient.GUIBootClass;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerSettingDialogController extends VBox implements Initializable
{

    public TextField tPOP3Ad;
    public TextField tPOP3Pt;
    public TextField tSMTPAd;
    public TextField tSMTPPt;

    private Stage thisStage;

    public void confirmAct()
    {
        CoreResources.config.mail_pop_server_address = tPOP3Ad.getText();
        CoreResources.config.mail_pop_server_port = tPOP3Pt.getText();
        CoreResources.config.mail_smtp_server_address = tSMTPAd.getText();
        CoreResources.config.mail_smtp_server_port = tSMTPPt.getText();
        thisStage.close();
    }

    public void cancelAct()
    {
        thisStage.close();
    }

    public ServerSettingDialogController(Stage thisStage)
    {
        this.thisStage = thisStage;

        FXMLLoader fxmlLoader = new FXMLLoader(GUIBootClass.class.getResource("fxml/server-setting-view.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        } catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        tPOP3Ad.setText(CoreResources.config.mail_pop_server_address);
        tPOP3Pt.setText(CoreResources.config.mail_pop_server_port);
        tSMTPAd.setText(CoreResources.config.mail_smtp_server_address);
        tSMTPPt.setText(CoreResources.config.mail_smtp_server_port);
    }
}
