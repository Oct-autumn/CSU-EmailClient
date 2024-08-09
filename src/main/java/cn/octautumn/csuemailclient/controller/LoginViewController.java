package cn.octautumn.csuemailclient.controller;

import cn.octautumn.csuemailclient.CoreResources;
import cn.octautumn.csuemailclient.GUIBootClass;
import cn.octautumn.csuemailclient.controller.customcontrols.ServerSettingDialogController;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.mail.*;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static cn.octautumn.csuemailclient.CoreResources.props;

public class LoginViewController implements Initializable
{
    public HBox mainHBox;

    public Button buttonExit;
    public Button buttonMinimize;
    public TextField tfUserAccount;
    public PasswordField tfUserPassword;
    public CheckBox cbRememberPwd;
    public Text tMailServerSet;
    public Button buttonLogin;

    public void showMailServerSettingAct()
    {
        Stage settingDialog = new Stage();
        settingDialog.initOwner(GUIBootClass.loginStage);
        settingDialog.initModality(Modality.WINDOW_MODAL);
        settingDialog.setScene(new Scene(new ServerSettingDialogController(settingDialog)));
        settingDialog.getIcons().add(new Image(Objects.requireNonNull(GUIBootClass.class.getResourceAsStream("img/icon.png"))));
        settingDialog.show();
    }

    public void loginAction()
    {
        {//非空检查
            boolean isComplete = true;
            if (tfUserAccount.getText().equals(""))
            {
                tfUserAccount.requestFocus();  //获取焦点
                tfUserAccount.getStyleClass().set(2, "text-field-status-error");
                isComplete = false;
            }
            if (tfUserPassword.getText().equals(""))
            {
                if (isComplete) tfUserPassword.requestFocus(); //获取焦点
                tfUserPassword.getStyleClass().set(2, "text-field-status-error");
                isComplete = false;
            }
            if (!isComplete) return;
        }

        if (cbRememberPwd.isSelected()) CoreResources.savePwd = true;

        CoreResources.config.user_account = tfUserAccount.getText();
        CoreResources.config.user_password = tfUserPassword.getText();

        props.setProperty("mail.store.protocol", "pop3");
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.pop3.host", CoreResources.config.mail_pop_server_address);
        props.setProperty("mail.smtp.host", CoreResources.config.mail_smtp_server_address);
        props.setProperty("mail.smtp.auth", "true");

        //创建session
        CoreResources.session = Session.getInstance(props);

        try
        {
            Store store = CoreResources.session.getStore();
            store.connect(
                    CoreResources.config.user_account,
                    CoreResources.config.user_password
            );
            store.close();
        } catch (MessagingException e)
        {//若失败
            if (e.getMessage().equals("Unable to log on"))
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setContentText("登陆失败，请检查您的账号密码是否正确！");
                alert.show();
            } else
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setContentText("发生未知错误，错误信息：\n" + e.getMessage());
                alert.show();
            }
            return;
        }


        GUIBootClass.loginStage.close();
        GUIBootClass.mainStage.show();

        GUIBootClass.mainViewController.refreshInboxAct();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        Stage thisStage = GUIBootClass.loginStage;

        {//退出按钮 样式设定
            buttonExit.getStyleClass().clear();
            buttonExit.getStyleClass().add("button-close-base");
            buttonExit.getStyleClass().add("button-close-normal");
            buttonExit.setOnMouseEntered(mouseEvent -> buttonExit.getStyleClass().set(1, "button-close-mouse-on"));
            buttonExit.setOnMousePressed(mouseEvent -> buttonExit.getStyleClass().set(1, "button-close-mouse-pressed"));
            buttonExit.setOnMouseExited(mouseEvent -> buttonExit.getStyleClass().set(1, "button-close-normal"));
            buttonExit.setOnMouseReleased(mouseEvent -> {
                if (buttonExit.isHover())
                    buttonExit.getStyleClass().set(1, "button-close-mouse-on");
                else
                    buttonExit.getStyleClass().set(1, "button-close-normal");
            });

            buttonExit.setOnAction(actionEvent -> System.exit(0));
        }
        {//最小化按钮 样式设定
            buttonMinimize.getStyleClass().clear();
            buttonMinimize.getStyleClass().add("button-minimize-base");
            buttonMinimize.getStyleClass().add("button-minimize-normal");
            buttonMinimize.setOnMouseEntered(mouseEvent -> buttonMinimize.getStyleClass().set(1, "button-minimize-mouse-on"));
            buttonMinimize.setOnMouseExited(mouseEvent -> buttonMinimize.getStyleClass().set(1, "button-minimize-normal"));
            buttonMinimize.setOnMousePressed(mouseEvent -> buttonMinimize.getStyleClass().set(1, "button-minimize-mouse-pressed"));
            buttonMinimize.setOnMouseReleased(mouseEvent -> {
                if (buttonMinimize.isHover())
                    buttonMinimize.getStyleClass().set(1, "button-minimize-mouse-on");
                else
                    buttonMinimize.getStyleClass().set(1, "button-minimize-normal");
            });

            buttonMinimize.setOnAction(actionEvent -> GUIBootClass.loginStage.setIconified(true));
        }

        {//登录按钮 样式设定
            buttonLogin.getStyleClass().clear();
            buttonLogin.getStyleClass().add("button-login-base");
            buttonLogin.getStyleClass().add("button-login-normal");
            buttonLogin.setOnMouseEntered(mouseEvent -> buttonLogin.getStyleClass().set(1, "button-login-mouse-on"));
            buttonLogin.setOnMouseExited(mouseEvent -> buttonLogin.getStyleClass().set(1, "button-login-normal"));
            buttonLogin.setOnMousePressed(mouseEvent -> buttonLogin.getStyleClass().set(1, "button-login-mouse-pressed"));
            buttonLogin.setOnMouseReleased(mouseEvent -> {
                if (buttonLogin.isHover())
                    buttonLogin.getStyleClass().set(1, "button-login-mouse-on");
                else
                    buttonLogin.getStyleClass().set(1, "button-login-normal");
            });
        }

        {//拖拽标题栏重定位窗口
            AtomicReference<Double> mouseX_ori = new AtomicReference<>((double) 0);
            AtomicReference<Double> mouseY_ori = new AtomicReference<>((double) 0);
            AtomicReference<Double> stageX_ori = new AtomicReference<>((double) 0);
            AtomicReference<Double> stageY_ori = new AtomicReference<>((double) 0);

            mainHBox.setOnMousePressed(mouseEvent -> {
                mouseX_ori.set(mouseEvent.getScreenX());
                mouseY_ori.set(mouseEvent.getScreenY());
                stageX_ori.set(thisStage.getX());
                stageY_ori.set(thisStage.getY());
            });
            mainHBox.setOnMouseDragged(mouseEvent -> {//移动窗口
                double finalX = stageX_ori.get() + mouseEvent.getScreenX() - mouseX_ori.get();
                double finalY = stageY_ori.get() + mouseEvent.getScreenY() - mouseY_ori.get();
                finalX = Math.max(0, finalX);
                finalX = Math.min(GUIBootClass.screenBounds.getMaxX() - thisStage.getWidth(), finalX);
                finalY = Math.max(0, finalY);
                finalY = Math.min(GUIBootClass.screenBounds.getMaxY() - 25, finalY);
                thisStage.setX(finalX);
                thisStage.setY(finalY);
            });
        }

        setTextFieldFocusStyle(tfUserAccount);
        setTextFieldFocusStyle(tfUserPassword);

        tfUserAccount.setText(CoreResources.config.user_account);
        tfUserPassword.setText(CoreResources.config.user_password);
        cbRememberPwd.setSelected(CoreResources.savePwd);
    }

    private void setTextFieldFocusStyle(TextField textField)
    {
        //初始化样式
        textField.getStyleClass().clear();
        textField.getStyleClass().addAll("text-field-base", "text-field-normal", "text-field-status-unfocused");

        textField.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (textField.getText().equals(""))
                textField.getStyleClass().set(2, "text-field-status-warning");
            else if (t1)
                textField.getStyleClass().set(2, "text-field-status-focused");
            else
                textField.getStyleClass().set(2, "text-field-status-unfocused");
        });

        textField.textProperty().addListener((observableValue, s, t1) -> {
            if (t1.isEmpty())
                textField.getStyleClass().set(2, "text-field-status-warning");
            else if (textField.isFocused())
                textField.getStyleClass().set(2, "text-field-status-focused");
            else
                textField.getStyleClass().set(2, "text-field-status-unfocused");
        });

        textField.disabledProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1)
                textField.getStyleClass().set(1, "text-field-disabled");
            else
                textField.getStyleClass().set(1, "text-field-normal");
        });
    }
}