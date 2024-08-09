package cn.octautumn.csuemailclient.config;

import cn.octautumn.csuemailclient.CoreResources;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;

import java.io.*;

import static cn.octautumn.csuemailclient.CoreResources.config;

public class ConfigurationOp
{
    public static void createNewConfig()
    {
        config.user_account = "";
        config.user_password = "";
        config.mail_pop_server_address = "pop.csu.edu.cn";
        config.mail_pop_server_port = "110";
        config.mail_smtp_server_address = "smtp.csu.edu.cn";
        config.mail_smtp_server_port = "25";
    }

    public static void saveConfig()
    {
        File configFile = new File("./config.json");
        ObjectMapper mapper = new ObjectMapper();
        BufferedWriter writer;

        if (!CoreResources.savePwd) config.user_password = "";

        try
        {
            writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(mapper.writeValueAsString(config));
            writer.close();
        } catch (IOException e)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("出错了");
            alert.setContentText("无法将配置保存到配置文件，如果继续运行，\n您所做的一切设置修改将不能保存。");
            alert.showAndWait();
        }
    }

    public static void loadConfig()
    {
        File configFile = new File("./config.json");
        if (!configFile.exists())
        {
            try
            {
                configFile.createNewFile();
            } catch (IOException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("出错了");
                alert.setContentText("无法创建配置文件！程序将自动退出");
                alert.showAndWait();
                System.exit(-1);
            }
            createNewConfig();
            saveConfig();
        }

        File configFile1 = new File("./config.json");

        ObjectMapper mapper = new ObjectMapper();
        try
        {
            config = mapper.readValue(configFile1, Config.class);
            if (!config.user_password.equals("")) CoreResources.savePwd = true;
        } catch (IOException e)
        {
            e.printStackTrace();
            createNewConfig();
            saveConfig();
        }
    }
}
