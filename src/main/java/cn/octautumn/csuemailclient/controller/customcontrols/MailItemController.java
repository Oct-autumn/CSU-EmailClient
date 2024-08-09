package cn.octautumn.csuemailclient.controller.customcontrols;

import cn.octautumn.csuemailclient.GUIBootClass;
import cn.octautumn.csuemailclient.common.Mail;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import lombok.Getter;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MailItemController extends HBox implements Initializable
{
    public Text textSubject;
    public Text textFrom;

    public int index;

    @Getter
    private final Mail mail;
    private final String mailSubject;
    private final String mailFrom;

    public void mouseClickAct()
    {
        GUIBootClass.mainViewController.vboxViewMailArea.setDisable(false);
        //加载头部信息
        GUIBootClass.mainViewController.textReceiver.setText(mail.getReceiver());
        GUIBootClass.mainViewController.textSender.setText(mail.getSender());
        GUIBootClass.mainViewController.textSubject.setText(mail.getSubject());
        GUIBootClass.mainViewController.textTime.setText(mail.getDate());
        //加载正文
        GUIBootClass.mainViewController.mailView.getEngine().loadContent(mail.getContent());
        //加载附件
        if (!mail.getAttachFiles().isEmpty())
        {
            GUIBootClass.mainViewController.hboxReceivedAppendixList.getChildren().clear();
            mail.getAttachFiles().forEach((s, bodyPart) -> {
                Text textFileName = new Text(s);
                textFileName.getStyleClass().add("text-attach");
                textFileName.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getClickCount() != 1) return;

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
                    alert.setContentText("附件下载完成！");
                    alert.showAndWait();
                });
                GUIBootClass.mainViewController.hboxReceivedAppendixList.getChildren().add(textFileName);
            });
        } else
        {
            GUIBootClass.mainViewController.hboxReceivedAppendixList.getChildren().clear();
            Text textFileName = new Text("（此邮件没有附件）");
            textFileName.getStyleClass().add("text-none-attach");
            GUIBootClass.mainViewController.hboxReceivedAppendixList.getChildren().add(textFileName);
        }

        GUIBootClass.mainViewController.setShowingMailIndex(mail.getIndex());
    }

    public MailItemController(Mail mail) throws MessagingException
    {
        this.mail = mail;
        this.mailSubject = mail.getSubject();
        this.mailFrom = mail.getSender();
        this.index = mail.getIndex();

        FXMLLoader fxmlLoader = new FXMLLoader(GUIBootClass.class.getResource("fxml/mail-item-control.fxml"));

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
        this.getStyleClass().clear();
        this.getStyleClass().add("main-HBox-base");
        this.getStyleClass().add("main-HBox-normal");
        this.setOnMouseEntered(mouseEvent -> this.getStyleClass().set(1, "main-HBox-mouse-on"));
        this.setOnMouseExited(mouseEvent -> this.getStyleClass().set(1, "main-HBox-normal"));
        this.setOnMousePressed(mouseEvent -> this.getStyleClass().set(1, "main-HBox-mouse-pressed"));
        this.setOnMouseReleased(mouseEvent -> {
            if (this.isHover())
                this.getStyleClass().set(1, "main-HBox-mouse-on");
            else
                this.getStyleClass().set(1, "main-HBox-normal");
        });
        HBox.setHgrow(this, Priority.ALWAYS);

        textSubject.setText(mailSubject);
        textFrom.setText(mailFrom);
    }
}
