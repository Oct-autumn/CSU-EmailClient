package cn.octautumn.csuemailclient;

import cn.octautumn.csuemailclient.config.Config;
import lombok.Data;

import javax.mail.Session;
import java.util.Properties;

@Data
public class CoreResources
{
    public static Config config = new Config();
    public static boolean savePwd = false;
    public static final Properties props = new Properties();
    public static Session session;
}
