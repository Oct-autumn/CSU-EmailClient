package cn.octautumn.csuemailclient;

import cn.octautumn.csuemailclient.config.ConfigurationOp;
import cn.octautumn.csuemailclient.controller.LoginViewController;
import cn.octautumn.csuemailclient.controller.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class GUIBootClass extends Application
{
    public static Scene loginScene;
    public static Scene mainScene;

    public static Stage loginStage;
    public static Stage mainStage;

    public static LoginViewController loginViewController;
    public static MainViewController mainViewController;

    public static final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

    @Override
    public void start(Stage stage) throws IOException
    {
        ConfigurationOp.loadConfig();

        {
            loginStage = stage;

            FXMLLoader fxmlLoader = new FXMLLoader(GUIBootClass.class.getResource("fxml/login-view.fxml"));
            loginScene = new Scene(fxmlLoader.load());
            loginViewController = fxmlLoader.getController();
            loginScene.setFill(Color.TRANSPARENT);

            stage.getIcons().add(new Image(Objects.requireNonNull(GUIBootClass.class.getResourceAsStream("img/icon.png"))));
            stage.setResizable(false);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(loginScene);
            stage.setMinHeight(loginScene.getHeight());
            stage.setMinWidth(loginScene.getWidth());
            stage.setTitle("中南大学电子邮件客户端 登录");
            stage.show();
        }

        {
            mainStage = new Stage();

            FXMLLoader fxmlLoader = new FXMLLoader(GUIBootClass.class.getResource("fxml/main-view.fxml"));
            mainScene = new Scene(fxmlLoader.load());
            mainViewController = fxmlLoader.getController();
            mainScene.setFill(Color.TRANSPARENT);

            mainStage.getIcons().add(new Image(Objects.requireNonNull(GUIBootClass.class.getResourceAsStream("img/icon.png"))));
            mainStage.initStyle(StageStyle.TRANSPARENT);
            mainStage.setScene(mainScene);
            mainStage.setMinHeight(mainScene.getHeight());
            mainStage.setMinWidth(mainScene.getWidth());
            mainStage.setTitle("中南大学电子邮件客户端");
        }

    }

    public static void main(String[] args)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(ConfigurationOp::saveConfig));

        launch();
    }
}