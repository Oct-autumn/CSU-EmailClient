package cn.octautumn.csuemailclient.controller;

import cn.octautumn.csuemailclient.CoreResources;
import cn.octautumn.csuemailclient.GUIBootClass;
import cn.octautumn.csuemailclient.common.Mail;
import cn.octautumn.csuemailclient.controller.customcontrols.MailItemController;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Setter;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static cn.octautumn.csuemailclient.CoreResources.session;
import static java.lang.Math.min;

public class MainViewController implements Initializable
{
    public HBox titleBar;

    public Button buttonMinimize;
    public Button buttonMaximize;
    public Button buttonExit;
    public SplitPane splitPane;
    public ScrollPane spMailList;
    public VBox vboxMailList;
    public VBox vboxViewMailArea;
    public Text textReceiver;
    public Text textSender;
    public Text textSubject;
    public Text textTime;
    public HBox hboxReceivedAppendixList;
    public WebView mailView;
    public TextField tfSenderAddress;
    public TextField tfReceiverAddress;
    public TextField tfCopyAddress;
    public TextField tfSubject;
    public HBox hboxToSendAppendixList;
    public HTMLEditor mailEditor;

    @Setter
    private int showingMailIndex = -1;

    private final ArrayList<File> attachFileList = new ArrayList<>();

    public void refreshInboxAct()
    {
        Platform.runLater(() -> {
            try
            {
                Store store = CoreResources.session.getStore();
                store.connect(
                        CoreResources.config.user_account,
                        CoreResources.config.user_password
                );

                Folder folder = store.getFolder("inbox");
                folder.open(Folder.READ_ONLY);

                vboxMailList.getChildren().clear();
                // 获得邮件夹Folder内的所有邮件Message对象
                Message[] messages = folder.getMessages();
                double ch = spMailList.getHeight();
                double totalH = 0;
                for (int i = 0; i < messages.length; i++)
                {
                    MailItemController mailItem = new MailItemController(new Mail(messages[i], i));
                    vboxMailList.getChildren().add(0, mailItem);
                    if (i == 0)
                        totalH = 60;
                    else
                        totalH += 53;
                }
                vboxMailList.setPrefHeight(totalH);
                vboxMailList.setMaxHeight(totalH);

                folder.close(true);
                store.close();
            } catch (MessagingException e)
            {//若失败
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setContentText("发生未知错误，请重新登录，错误信息：\n" + e.getMessage());
                alert.showAndWait();

                GUIBootClass.mainStage.close();
                GUIBootClass.loginStage.show();
            } catch (IOException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setContentText("发生未知错误，获取邮件失败，错误信息：\n" + e.getMessage());
                alert.showAndWait();
            }

            mailView.getEngine().loadContent("");
            hboxReceivedAppendixList.getChildren().clear();
            textReceiver.setText("");
            textSender.setText("");
            textSubject.setText("");
            textTime.setText("");
            setShowingMailIndex(-1);
        });
    }

    public void delMailAct()
    {
        if (showingMailIndex == -1) return;

        Platform.runLater(() -> {
            try
            {
                Store store = CoreResources.session.getStore();
                store.connect(
                        CoreResources.config.user_account,
                        CoreResources.config.user_password
                );

                Folder folder = store.getFolder("inbox");
                folder.open(Folder.READ_WRITE);

                Message message = folder.getMessage(showingMailIndex + 1);
                message.setFlag(Flags.Flag.DELETED, true);
                folder.close(true);
                store.close();
            } catch (MessagingException e)
            {//若失败
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setContentText("发生未知错误，请重新登录，错误信息：\n" + e.getMessage());
                alert.showAndWait();

                GUIBootClass.mainStage.close();
                GUIBootClass.loginStage.show();
            }

            mailView.getEngine().loadContent("");
            hboxReceivedAppendixList.getChildren().clear();
            setShowingMailIndex(-1);
            vboxViewMailArea.setDisable(true);
        });

        refreshInboxAct();
    }

    public void downloadAllAppendixAct()
    {
        if (showingMailIndex == -1) return;

        Mail mail = null;
        for (Node it : vboxMailList.getChildren())
            if (((MailItemController) it).index == showingMailIndex)
                mail = ((MailItemController) it).getMail();

        mail.getAttachFiles().forEach((s, bodyPart) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存附件");
            fileChooser.setInitialFileName(s);
            fileChooser.setInitialDirectory(new File("./"));
            File file = fileChooser.showSaveDialog(GUIBootClass.mainStage);
            if (file == null)
                return;
            BufferedOutputStream bos = null;
            BufferedInputStream bis = null;
            try
            {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bis = new BufferedInputStream(bodyPart.getInputStream());
                int c;
                while ((c = bis.read()) != -1)
                {
                    bos.write(c);
                    bos.flush();
                }
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            } catch (MessagingException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setContentText("发生未知错误，下载附件出错，错误信息：\n" + e.getMessage());
                alert.showAndWait();
            }
            try
            {
                bos.close();
                bis.close();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("完成");
            alert.setContentText("附件\"" + s + "\"下载完成！");
            alert.showAndWait();
        });
    }

    public void sendMailAct()
    {
        {//非空检查
            boolean isComplete = true;
            if (tfSenderAddress.getText().equals(""))
            {
                tfSenderAddress.requestFocus();  //获取焦点
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("警告");
                alert.setContentText("收件人地址不能为空");
                alert.showAndWait();
                isComplete = false;
            }
            if (tfReceiverAddress.getText().equals(""))
            {
                if (isComplete) tfReceiverAddress.requestFocus(); //获取焦点
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("警告");
                alert.setContentText("收件人地址不能为空");
                alert.showAndWait();
                isComplete = false;
            }
            if (tfSubject.getText().equals(""))
            {
                if (isComplete) tfSubject.requestFocus(); //获取焦点
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("警告");
                alert.setContentText("邮件主题不能为空");
                alert.showAndWait();
                isComplete = false;
            }
            if (!isComplete) return;
        }

        try
        {
            // 2、通过session得到transport对象
            Transport ts = session.getTransport();

            // 3、连上邮件服务器
            ts.connect(CoreResources.config.user_account
                    , CoreResources.config.user_password);

            // 4、创建邮件
            MimeMessage message = new MimeMessage(session);

            // 邮件消息头
            message.setFrom(new InternetAddress(tfSenderAddress.getText())); // 邮件的发件人
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(tfReceiverAddress.getText())); // 邮件的收件人
            if (!tfCopyAddress.getText().equals(""))
                message.setRecipient(Message.RecipientType.CC, new InternetAddress(tfCopyAddress.getText())); // 邮件的抄送人
            message.setSubject(tfSubject.getText()); // 邮件的标题

            MimeBodyPart text = new MimeBodyPart();
            text.setContent(mailEditor.getHtmlText(), "text/html;charset=UTF-8");

            // 描述数据关系
            MimeMultipart mm = new MimeMultipart();
            mm.setSubType("related");
            mm.addBodyPart(text);

            // 添加邮件附件
            for (File file : attachFileList)
            {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(file);
                mm.addBodyPart(attachPart);
            }

            message.setContent(mm);
            message.saveChanges();

            // 5、发送邮件
            ts.sendMessage(message, message.getAllRecipients());
            ts.close();
        } catch (MessagingException e)
        {//若失败
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setContentText("发生未知错误，请重新登录，错误信息：\n" + e.getMessage());
            alert.showAndWait();

            GUIBootClass.mainStage.close();
            GUIBootClass.loginStage.show();
        } catch (IOException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setContentText("发生未知错误，附件添加失败，错误信息：\n" + e.getMessage());
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setContentText("邮件已成功发送");
        alert.showAndWait();
        tfSenderAddress.setText(CoreResources.config.user_account);
        tfReceiverAddress.setText("");
        tfCopyAddress.setText("");
        tfSubject.setText("");
        mailEditor.setHtmlText("");
        attachFileList.clear();
        hboxToSendAppendixList.getChildren().clear();
        Text textFileName = new Text("（将附件拖动至此以添加）");
        textFileName.getStyleClass().add("text-none-attach");
        hboxToSendAppendixList.getChildren().add(textFileName);
    }

    public void dragOverAct(DragEvent event)
    {
        if (event.getGestureSource() != hboxToSendAppendixList)
        {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    public void dragDroppedAct(DragEvent event)
    {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles())
        {
            try
            {
                File file = dragboard.getFiles().get(0);
                if (file != null)
                {
                    if (((Text) hboxToSendAppendixList.getChildren().get(0)).getText().equals("（将附件拖动至此以添加）"))
                        hboxToSendAppendixList.getChildren().clear();
                    Text textFileName = new Text(file.getName());
                    textFileName.setOnMouseClicked(mouseEvent -> {
                        if (mouseEvent.getClickCount() < 2) return;
                        hboxToSendAppendixList.getChildren().remove(textFileName);
                        attachFileList.remove(file.getAbsoluteFile());
                        if (hboxToSendAppendixList.getChildren().isEmpty())
                        {
                            Text text = new Text("（将附件拖动至此以添加）");
                            text.getStyleClass().add("text-none-attach");
                            hboxToSendAppendixList.getChildren().add(text);
                        }
                    });
                    textFileName.getStyleClass().add("text-attach");
                    hboxToSendAppendixList.getChildren().add(textFileName);
                    attachFileList.add(file.getAbsoluteFile());
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setContentText("发生未知错误，附件添加失败，错误信息：\n" + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        Stage thisStage = GUIBootClass.mainStage;

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

            GUIBootClass.mainStage.setOnCloseRequest((event) -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("退出");
                alert.setContentText("确定退出？");
                alert.showAndWait();
                if (alert.getResult().equals(ButtonType.OK)) System.exit(0);
                event.consume();
            }); //关闭窗口时自动停止所有进程并退出

            buttonExit.setOnAction(actionEvent ->
                    GUIBootClass.mainStage.fireEvent(new WindowEvent(GUIBootClass.mainStage, WindowEvent.WINDOW_CLOSE_REQUEST)));
        }
        {//最大化按钮 样式设定
            buttonMaximize.getStyleClass().clear();
            buttonMaximize.getStyleClass().add("button-maximize-base");
            buttonMaximize.getStyleClass().add("button-maximize-normal");
            buttonMaximize.setOnMouseEntered(mouseEvent -> buttonMaximize.getStyleClass().set(1, "button-maximize-mouse-on"));
            buttonMaximize.setOnMousePressed(mouseEvent -> buttonMaximize.getStyleClass().set(1, "button-maximize-mouse-pressed"));
            buttonMaximize.setOnMouseExited(mouseEvent -> buttonMaximize.getStyleClass().set(1, "button-maximize-normal"));
            buttonMaximize.setOnMouseReleased(mouseEvent -> {
                if (buttonMaximize.isHover())
                    buttonMaximize.getStyleClass().set(1, "button-maximize-mouse-on");
                else
                    buttonMaximize.getStyleClass().set(1, "button-maximize-normal");
            });

            buttonMaximize.setOnAction(actionEvent ->
                    GUIBootClass.mainStage.setMaximized(!GUIBootClass.mainStage.isMaximized()));
        }
        {//最小化按钮 样式设定
            buttonMinimize.getStyleClass().clear();
            buttonMinimize.getStyleClass().add("button-minimize-base");
            buttonMinimize.getStyleClass().add("button-minimize-normal");
            buttonMinimize.setOnMouseEntered(mouseEvent -> buttonMinimize.getStyleClass().set(1, "button-minimize-mouse-on"));
            buttonMinimize.setOnMousePressed(mouseEvent -> buttonMinimize.getStyleClass().set(1, "button-minimize-mouse-pressed"));
            buttonMinimize.setOnMouseExited(mouseEvent -> buttonMinimize.getStyleClass().set(1, "button-minimize-normal"));
            buttonMinimize.setOnMouseReleased(mouseEvent -> {
                if (buttonMinimize.isHover())
                    buttonMinimize.getStyleClass().set(1, "button-minimize-mouse-on");
                else
                    buttonMinimize.getStyleClass().set(1, "button-minimize-normal");
            });

            buttonMinimize.setOnAction(actionEvent ->
                    GUIBootClass.mainStage.setIconified(true));
        }

        {//拖拽标题栏重定位窗口
            AtomicReference<Double> mouseX_ori = new AtomicReference<>((double) 0);
            AtomicReference<Double> mouseY_ori = new AtomicReference<>((double) 0);
            AtomicReference<Double> stageX_ori = new AtomicReference<>((double) 0);
            AtomicReference<Double> stageY_ori = new AtomicReference<>((double) 0);

            titleBar.setOnMousePressed(mouseEvent -> {
                mouseX_ori.set(mouseEvent.getScreenX());
                mouseY_ori.set(mouseEvent.getScreenY());
                stageX_ori.set(thisStage.getX());
                stageY_ori.set(thisStage.getY());
            });
            titleBar.setOnMouseDragged(mouseEvent -> {//移动窗口
                double finalX = stageX_ori.get() + mouseEvent.getScreenX() - mouseX_ori.get();
                double finalY = stageY_ori.get() + mouseEvent.getScreenY() - mouseY_ori.get();
                finalX = Math.max(0, finalX);
                finalX = min(GUIBootClass.screenBounds.getMaxX() - thisStage.getWidth(), finalX);
                finalY = Math.max(0, finalY);
                finalY = min(GUIBootClass.screenBounds.getMaxY() - 25, finalY);
                thisStage.setX(finalX);
                thisStage.setY(finalY);
            });
        }

        splitPane.heightProperty().addListener((observableValue, number, t1) -> spMailList.setMaxHeight((Double) t1));
        splitPane.widthProperty().addListener((observableValue, number, t1) -> {
            double w = (Double) t1 - 10;
            vboxMailList.getChildren().forEach(node -> {
                ((HBox) node).setPrefWidth(w);
                ((HBox) node).setMaxWidth(w);
            });
        });
        tfSenderAddress.setText(CoreResources.config.user_account);
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
